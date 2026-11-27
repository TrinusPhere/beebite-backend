package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncRejectedEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlushAndProbeEdgeSyncUseCaseTest {

    @Mock
    EdgeOutboxRepository outboxRepository;
    @Mock
    EdgeSyncStreamStateRepository streamStateRepository;
    @Mock
    EdgeCloudSyncClient cloudClient;

    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private EdgeSyncAckHandler ackHandler;
    private FlushEdgeOutboxUseCase flushUseCase;
    private ProbeEdgeSyncCursorUseCase probeUseCase;

    @BeforeEach
    void setUp() {
        ackHandler = new EdgeSyncAckHandler(outboxRepository, streamStateRepository);
        flushUseCase = new FlushEdgeOutboxUseCase(
                outboxRepository,
                streamStateRepository,
                cloudClient,
                ackHandler,
                "EDGE",
                "edge-token",
                Duration.ofSeconds(15)
        );
        probeUseCase = new ProbeEdgeSyncCursorUseCase(
                outboxRepository,
                streamStateRepository,
                cloudClient,
                ackHandler,
                "EDGE",
                Optional.of(STORE_ID.toString()),
                "edge-token",
                Duration.ofSeconds(15)
        );
    }

    @Test
    void shouldMarkSyncedMarkRejectedRecordUnackedAndReopenFalseSyncedEvents() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        EdgeOutboxEvent ev1 = EdgeOutboxEvent.reconstitute(
                id1, STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{}",
                EdgeOutboxStatus.PENDING, 0, null, Instant.now(), null, 10L, null);
        EdgeOutboxEvent ev2 = EdgeOutboxEvent.reconstitute(
                id2, STORE_ID, "FOO_UPSERT", UUID.randomUUID(), "{}",
                EdgeOutboxStatus.PENDING, 0, null, Instant.now(), null, 11L, null);
        EdgeOutboxEvent ev3 = EdgeOutboxEvent.reconstitute(
                id3, STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{}",
                EdgeOutboxStatus.PENDING, 0, null, Instant.now(), null, 12L, null);

        EdgeSyncStreamState state = EdgeSyncStreamState.initial(STORE_ID);
        when(outboxRepository.findStoreIdsWithPending()).thenReturn(List.of(STORE_ID));
        when(streamStateRepository.findOrInitialize(STORE_ID)).thenReturn(state);
        when(outboxRepository.findPendingBatch(STORE_ID, 50)).thenReturn(List.of(ev1, ev2, ev3));
        when(outboxRepository.findMinRetainedSequence(STORE_ID)).thenReturn(Optional.of(1L));

        EdgeSyncBatchResponse cloudResponse = new EdgeSyncBatchResponse(
                1,
                List.of(id1),
                List.of(new EdgeSyncRejectedEventDto(id2, "Tipo desconhecido")),
                8L,
                true,
                Instant.now()
        );
        when(cloudClient.sendBatch(any(EdgeSyncBatchRequest.class), eq("edge-token"))).thenReturn(cloudResponse);
        when(outboxRepository.reopenSyncedAfter(STORE_ID, 8L)).thenReturn(2);

        int synced = flushUseCase.execute();

        assertEquals(1, synced);
        verify(outboxRepository).markSynced(eq(List.of(id1)), any(Instant.class));
        verify(outboxRepository).markRejected(id2, "Tipo desconhecido");
        verify(outboxRepository).recordTransientFailure(eq(List.of(id3)), any());
        verify(outboxRepository).reopenSyncedAfter(STORE_ID, 8L);
        assertTrue(state.isFullResyncRequested());
        assertEquals(8L, state.getCloudAckSequence());
        verify(streamStateRepository).save(state);
    }

    @Test
    void shouldSkipFlushWhenInBackoffAndEnterBackoffOnNetworkFailure() {
        EdgeSyncStreamState state = EdgeSyncStreamState.initial(STORE_ID);
        UUID id1 = UUID.randomUUID();
        EdgeOutboxEvent ev1 = EdgeOutboxEvent.reconstitute(
                id1, STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{}",
                EdgeOutboxStatus.PENDING, 0, null, Instant.now(), null, 1L, null);

        when(outboxRepository.findStoreIdsWithPending()).thenReturn(List.of(STORE_ID));
        when(streamStateRepository.findOrInitialize(STORE_ID)).thenReturn(state);
        when(outboxRepository.findPendingBatch(STORE_ID, 50)).thenReturn(List.of(ev1));
        when(outboxRepository.findMinRetainedSequence(STORE_ID)).thenReturn(Optional.of(1L));
        when(cloudClient.sendBatch(any(), any())).thenThrow(new RuntimeException("Connection refused"));

        assertEquals(0, flushUseCase.execute());
        assertEquals(1, state.getConsecutiveFailures());
        assertNotNull(state.getNextAttemptAt());
        verify(outboxRepository).recordTransientFailure(eq(List.of(id1)), contains("Connection refused"));

        // Chamada imediata seguinte deve ser pulada por backoff
        reset(cloudClient);
        assertEquals(0, flushUseCase.execute());
        verify(cloudClient, never()).sendBatch(any(), any());
    }

    @Test
    void shouldProbeCloudCursorAndReopenFalseSyncedEventsWhenIdle() {
        EdgeSyncStreamState state = EdgeSyncStreamState.initial(STORE_ID);
        when(streamStateRepository.findOrInitialize(STORE_ID)).thenReturn(state);
        when(outboxRepository.findMinRetainedSequence(STORE_ID)).thenReturn(Optional.of(1L));
        when(cloudClient.sendBatch(any(EdgeSyncBatchRequest.class), eq("edge-token")))
                .thenReturn(new EdgeSyncBatchResponse(0, List.of(), List.of(), 5L, false, Instant.now()));
        when(outboxRepository.reopenSyncedAfter(STORE_ID, 5L)).thenReturn(3);

        boolean reopened = probeUseCase.execute();

        assertTrue(reopened);
        verify(outboxRepository).reopenSyncedAfter(STORE_ID, 5L);
        assertEquals(5L, state.getCloudAckSequence());
    }
}


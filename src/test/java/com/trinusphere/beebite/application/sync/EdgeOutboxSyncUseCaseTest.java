package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.application.sync.port.EdgeEventInbox;
import com.trinusphere.beebite.application.sync.port.EdgeEventOutcome;
import com.trinusphere.beebite.domain.shared.exception.UnauthorizedException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
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
class EdgeOutboxSyncUseCaseTest {

    @Mock
    EdgeOutboxRepository edgeOutboxRepository;
    @Mock
    EdgeSyncStreamStateRepository streamStateRepository;
    @Mock
    EdgeCloudSyncClient edgeCloudSyncClient;
    @Mock
    StoreRepository storeRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EdgeEventInbox edgeEventInbox;
    @Mock
    EdgeSyncCursorRepository cursorRepository;

    private static final UUID STORE_BALCAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private Store balcaoStore;

    @BeforeEach
    void setUp() {
        balcaoStore = Store.reconstitute(
                STORE_BALCAO_ID,
                "Chefs Burguer - Balcão",
                null,
                true,
                null,
                "Av. Central, 200",
                "chefs-balcao",
                "EDGE",
                "$2a$10$hashedEdgeToken",
                null
        );
    }

    @Test
    void shouldEnqueueEventWhenNodeIsInEdgeMode() {
        EnqueueEdgeOutboxUseCase useCase = new EnqueueEdgeOutboxUseCase(edgeOutboxRepository, "EDGE");
        UUID orderId = UUID.randomUUID();

        when(edgeOutboxRepository.save(any(EdgeOutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<EdgeOutboxEvent> saved = useCase.execute(
                STORE_BALCAO_ID,
                "ORDER_UPSERT",
                orderId,
                "{\"displayId\":42,\"totalAmount\":59.90}"
        );

        assertTrue(saved.isPresent());
        assertEquals("ORDER_UPSERT", saved.get().getEventType());
        verify(edgeOutboxRepository, times(1)).save(any(EdgeOutboxEvent.class));
    }

    @Test
    void shouldSkipEnqueueWhenNodeIsInCloudMode() {
        EnqueueEdgeOutboxUseCase useCase = new EnqueueEdgeOutboxUseCase(edgeOutboxRepository, "CLOUD");

        Optional<EdgeOutboxEvent> saved = useCase.execute(
                STORE_BALCAO_ID,
                "ORDER_UPSERT",
                UUID.randomUUID(),
                "{\"displayId\":42}"
        );

        assertTrue(saved.isEmpty());
        verify(edgeOutboxRepository, never()).save(any());
    }

    @Test
    void shouldFlushPendingEventsAndMarkSyncedWhenOnline() {
        EdgeSyncAckHandler ackHandler = new EdgeSyncAckHandler(edgeOutboxRepository, streamStateRepository);
        FlushEdgeOutboxUseCase useCase = new FlushEdgeOutboxUseCase(
                edgeOutboxRepository, streamStateRepository, edgeCloudSyncClient, ackHandler,
                "EDGE", "edge-secret-token", Duration.ofSeconds(15));

        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();
        EdgeOutboxEvent ev1 = EdgeOutboxEvent.reconstitute(
                eventId1, STORE_BALCAO_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"id\":1}",
                null, 0, null, Instant.now(), null, 1L, null);
        EdgeOutboxEvent ev2 = EdgeOutboxEvent.reconstitute(
                eventId2, STORE_BALCAO_ID, "CASH_SESSION_UPSERT", UUID.randomUUID(), "{\"id\":2}",
                null, 0, null, Instant.now(), null, 2L, null);

        when(edgeOutboxRepository.findStoreIdsWithPending()).thenReturn(List.of(STORE_BALCAO_ID));
        when(streamStateRepository.findOrInitialize(STORE_BALCAO_ID))
                .thenReturn(EdgeSyncStreamState.initial(STORE_BALCAO_ID));
        when(edgeOutboxRepository.findPendingBatch(STORE_BALCAO_ID, 50)).thenReturn(List.of(ev1, ev2));
        when(edgeOutboxRepository.findMinRetainedSequence(STORE_BALCAO_ID)).thenReturn(Optional.of(1L));
        when(edgeCloudSyncClient.sendBatch(any(EdgeSyncBatchRequest.class), eq("edge-secret-token")))
                .thenReturn(new EdgeSyncBatchResponse(2, List.of(eventId1, eventId2), Instant.now()));

        int syncedCount = useCase.execute();

        assertEquals(2, syncedCount);
        verify(edgeOutboxRepository).markSynced(eq(List.of(eventId1, eventId2)), any(Instant.class));
    }

    @Test
    void shouldKeepEventsPendingAndRecordFailureWhenInternetIsOffline() {
        EdgeSyncAckHandler ackHandler = new EdgeSyncAckHandler(edgeOutboxRepository, streamStateRepository);
        FlushEdgeOutboxUseCase useCase = new FlushEdgeOutboxUseCase(
                edgeOutboxRepository, streamStateRepository, edgeCloudSyncClient, ackHandler,
                "EDGE", "edge-secret-token", Duration.ofSeconds(15));

        UUID eventId = UUID.randomUUID();
        EdgeOutboxEvent ev = EdgeOutboxEvent.reconstitute(
                eventId, STORE_BALCAO_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"id\":1}",
                null, 0, null, Instant.now(), null, 1L, null);

        when(edgeOutboxRepository.findStoreIdsWithPending()).thenReturn(List.of(STORE_BALCAO_ID));
        when(streamStateRepository.findOrInitialize(STORE_BALCAO_ID))
                .thenReturn(EdgeSyncStreamState.initial(STORE_BALCAO_ID));
        when(edgeOutboxRepository.findPendingBatch(STORE_BALCAO_ID, 50)).thenReturn(List.of(ev));
        when(edgeOutboxRepository.findMinRetainedSequence(STORE_BALCAO_ID)).thenReturn(Optional.of(1L));
        when(edgeCloudSyncClient.sendBatch(any(), any()))
                .thenThrow(new RuntimeException("No route to host (Internet Offline)"));

        int syncedCount = useCase.execute();

        assertEquals(0, syncedCount);
        verify(edgeOutboxRepository, never()).markSynced(any(), any());
        verify(edgeOutboxRepository).recordTransientFailure(eq(List.of(eventId)), contains("Internet Offline"));
    }

    @Test
    void shouldIngestBatchIdempotentlyOnCloudAndUpdateStoreLastSyncTimestamp() {
        EdgeDeviceTokenValidator validator = new EdgeDeviceTokenValidator(passwordEncoder);
        IngestEdgeSyncBatchUseCase useCase = new IngestEdgeSyncBatchUseCase(
                storeRepository, validator, edgeEventInbox, cursorRepository);

        UUID newEventId = UUID.randomUUID();
        UUID duplicateEventId = UUID.randomUUID();
        EdgeSyncEventDto ev1 = new EdgeSyncEventDto(newEventId, "ORDER_UPSERT", UUID.randomUUID(), "{\"displayId\":10}", Instant.now());
        EdgeSyncEventDto ev2 = new EdgeSyncEventDto(duplicateEventId, "ORDER_UPSERT", UUID.randomUUID(), "{\"displayId\":11}", Instant.now());

        EdgeSyncBatchRequest request = new EdgeSyncBatchRequest(STORE_BALCAO_ID, List.of(ev1, ev2));

        when(storeRepository.findById(STORE_BALCAO_ID)).thenReturn(Optional.of(balcaoStore));
        when(passwordEncoder.matches("valid-edge-token", "$2a$10$hashedEdgeToken")).thenReturn(true);
        when(edgeEventInbox.applyLegacy(STORE_BALCAO_ID, ev1)).thenReturn(EdgeEventOutcome.applied(newEventId));
        when(edgeEventInbox.applyLegacy(STORE_BALCAO_ID, ev2)).thenReturn(EdgeEventOutcome.duplicate(duplicateEventId));

        EdgeSyncBatchResponse response = useCase.execute("valid-edge-token", request);

        assertEquals(1, response.processedCount());
        assertEquals(2, response.ackedEventIds().size());
        verify(storeRepository).save(balcaoStore);
        assertNotNull(balcaoStore.getLastEdgeSyncAt());
    }

    @Test
    void shouldRejectIngestBatchWhenEdgeTokenIsInvalid() {
        EdgeDeviceTokenValidator validator = new EdgeDeviceTokenValidator(passwordEncoder);
        IngestEdgeSyncBatchUseCase useCase = new IngestEdgeSyncBatchUseCase(
                storeRepository, validator, edgeEventInbox, cursorRepository);

        EdgeSyncBatchRequest request = new EdgeSyncBatchRequest(STORE_BALCAO_ID, List.of());

        when(storeRepository.findById(STORE_BALCAO_ID)).thenReturn(Optional.of(balcaoStore));
        when(passwordEncoder.matches("wrong-token", "$2a$10$hashedEdgeToken")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> useCase.execute("wrong-token", request));
        verifyNoInteractions(edgeEventInbox);
    }
}

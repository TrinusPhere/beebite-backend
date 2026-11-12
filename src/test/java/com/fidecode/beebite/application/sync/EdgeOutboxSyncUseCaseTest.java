package com.fidecode.beebite.application.sync;

import com.fidecode.beebite.application.auth.port.PasswordEncoder;
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.fidecode.beebite.application.sync.dto.EdgeSyncEventDto;
import com.fidecode.beebite.application.sync.port.EdgeCloudSyncClient;
import com.fidecode.beebite.application.sync.port.EdgeSnapshotApplier;
import com.fidecode.beebite.domain.shared.exception.UnauthorizedException;
import com.fidecode.beebite.domain.store.entity.Store;
import com.fidecode.beebite.domain.store.repository.StoreRepository;
import com.fidecode.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.fidecode.beebite.domain.sync.repository.EdgeOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    EdgeCloudSyncClient edgeCloudSyncClient;
    @Mock
    StoreRepository storeRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EdgeSnapshotApplier edgeSnapshotApplier;

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
        FlushEdgeOutboxUseCase useCase = new FlushEdgeOutboxUseCase(
                edgeOutboxRepository, edgeCloudSyncClient, "EDGE", "edge-secret-token");

        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();
        EdgeOutboxEvent ev1 = EdgeOutboxEvent.reconstitute(
                eventId1, STORE_BALCAO_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"id\":1}",
                null, 0, null, Instant.now(), null);
        EdgeOutboxEvent ev2 = EdgeOutboxEvent.reconstitute(
                eventId2, STORE_BALCAO_ID, "CASH_SESSION_UPSERT", UUID.randomUUID(), "{\"id\":2}",
                null, 0, null, Instant.now(), null);

        when(edgeOutboxRepository.findPendingBatch(50)).thenReturn(List.of(ev1, ev2));
        when(edgeCloudSyncClient.sendBatch(any(EdgeSyncBatchRequest.class), eq("edge-secret-token")))
                .thenReturn(new EdgeSyncBatchResponse(2, List.of(eventId1, eventId2), Instant.now()));

        int syncedCount = useCase.execute();

        assertEquals(2, syncedCount);
        verify(edgeOutboxRepository).markSynced(eq(List.of(eventId1, eventId2)), any(Instant.class));
    }

    @Test
    void shouldKeepEventsPendingAndRecordFailureWhenInternetIsOffline() {
        FlushEdgeOutboxUseCase useCase = new FlushEdgeOutboxUseCase(
                edgeOutboxRepository, edgeCloudSyncClient, "EDGE", "edge-secret-token");

        UUID eventId = UUID.randomUUID();
        EdgeOutboxEvent ev = EdgeOutboxEvent.reconstitute(
                eventId, STORE_BALCAO_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"id\":1}",
                null, 0, null, Instant.now(), null);

        when(edgeOutboxRepository.findPendingBatch(50)).thenReturn(List.of(ev));
        when(edgeCloudSyncClient.sendBatch(any(), any()))
                .thenThrow(new RuntimeException("No route to host (Internet Offline)"));

        int syncedCount = useCase.execute();

        assertEquals(0, syncedCount);
        verify(edgeOutboxRepository, never()).markSynced(any(), any());
        verify(edgeOutboxRepository).markFailed(eq(eventId), contains("Internet Offline"));
    }

    @Test
    void shouldIngestBatchIdempotentlyOnCloudAndUpdateStoreLastSyncTimestamp() {
        IngestEdgeSyncBatchUseCase useCase = new IngestEdgeSyncBatchUseCase(
                storeRepository, edgeOutboxRepository, passwordEncoder, edgeSnapshotApplier);

        UUID newEventId = UUID.randomUUID();
        UUID duplicateEventId = UUID.randomUUID();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        EdgeSyncBatchRequest request = new EdgeSyncBatchRequest(
                STORE_BALCAO_ID,
                List.of(
                        new EdgeSyncEventDto(newEventId, "ORDER_UPSERT", orderId1, "{\"displayId\":10}", Instant.now()),
                        new EdgeSyncEventDto(duplicateEventId, "ORDER_UPSERT", orderId2, "{\"displayId\":11}", Instant.now())
                )
        );

        when(storeRepository.findById(STORE_BALCAO_ID)).thenReturn(Optional.of(balcaoStore));
        when(passwordEncoder.matches("valid-edge-token", "$2a$10$hashedEdgeToken")).thenReturn(true);
        when(edgeOutboxRepository.isEventAlreadyProcessed(newEventId)).thenReturn(false);
        when(edgeOutboxRepository.isEventAlreadyProcessed(duplicateEventId)).thenReturn(true);

        EdgeSyncBatchResponse response = useCase.execute("valid-edge-token", request);

        assertEquals(1, response.processedCount());
        assertEquals(2, response.ackedEventIds().size());
        verify(edgeSnapshotApplier, times(1)).apply(STORE_BALCAO_ID, "ORDER_UPSERT", orderId1, "{\"displayId\":10}");
        verify(edgeSnapshotApplier, never()).apply(eq(STORE_BALCAO_ID), any(), eq(orderId2), any());
        verify(edgeOutboxRepository, times(1)).recordProcessedEvent(newEventId, STORE_BALCAO_ID, "ORDER_UPSERT", orderId1);
        verify(storeRepository).save(balcaoStore);
        assertNotNull(balcaoStore.getLastEdgeSyncAt());
    }

    @Test
    void shouldRejectIngestBatchWhenEdgeTokenIsInvalid() {
        IngestEdgeSyncBatchUseCase useCase = new IngestEdgeSyncBatchUseCase(
                storeRepository, edgeOutboxRepository, passwordEncoder, edgeSnapshotApplier);

        EdgeSyncBatchRequest request = new EdgeSyncBatchRequest(STORE_BALCAO_ID, List.of());

        when(storeRepository.findById(STORE_BALCAO_ID)).thenReturn(Optional.of(balcaoStore));
        when(passwordEncoder.matches("wrong-token", "$2a$10$hashedEdgeToken")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> useCase.execute("wrong-token", request));
        verify(edgeSnapshotApplier, never()).apply(any(), any(), any(), any());
    }
}

package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncHealthResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationResult;
import com.trinusphere.beebite.application.sync.port.EdgeOutboxRequeuePort;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.exception.EdgeOutboxEventNotRetryableException;
import com.trinusphere.beebite.domain.sync.exception.EdgeSyncBusyException;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeReconciliationRunRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncedEventLogRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeSyncAdminUseCasesTest {

    @Mock
    EdgeOutboxRepository outboxRepository;
    @Mock
    EdgeSyncedEventLogRepository eventLogRepository;
    @Mock
    EdgeSyncStreamStateRepository streamStateRepository;
    @Mock
    EdgeReconciliationRunRepository runRepository;
    @Mock
    EdgeOutboxRequeuePort requeuePort;
    @Mock
    ReconcileEdgeDataUseCase reconcileEdgeDataUseCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    void shouldPurgeAccordingToSyncMode() {
        when(outboxRepository.purgeSyncedBefore(any())).thenReturn(12);
        when(eventLogRepository.purgeReceivedBefore(any())).thenReturn(45);

        PurgeEdgeSyncHistoryUseCase edgePurge = new PurgeEdgeSyncHistoryUseCase(
                outboxRepository, eventLogRepository, "EDGE", 30, 90);
        assertEquals(12, edgePurge.execute());
        verify(eventLogRepository, never()).purgeReceivedBefore(any());

        PurgeEdgeSyncHistoryUseCase cloudPurge = new PurgeEdgeSyncHistoryUseCase(
                outboxRepository, eventLogRepository, "CLOUD", 30, 90);
        assertEquals(45, cloudPurge.execute());
    }

    @Test
    void shouldBuildHealthSummary() {
        when(outboxRepository.countByStatus(STORE_ID, EdgeOutboxStatus.PENDING)).thenReturn(3L);
        when(outboxRepository.countByStatus(STORE_ID, EdgeOutboxStatus.FAILED)).thenReturn(1L);
        when(outboxRepository.findOldestPendingCreatedAt(STORE_ID)).thenReturn(Optional.of(Instant.now().minusSeconds(40)));
        EdgeSyncStreamState state = EdgeSyncStreamState.initial(STORE_ID);
        state.recordSuccess(99L, Instant.now());
        when(streamStateRepository.findOrInitialize(STORE_ID)).thenReturn(state);
        when(runRepository.findLatestByStoreId(STORE_ID)).thenReturn(Optional.empty());

        GetEdgeSyncHealthUseCase healthUseCase = new GetEdgeSyncHealthUseCase(
                outboxRepository, streamStateRepository, runRepository, "EDGE");
        EdgeSyncHealthResponse health = healthUseCase.execute(STORE_ID);

        assertEquals("EDGE", health.syncMode());
        assertEquals(3L, health.pendingCount());
        assertEquals(1L, health.failedCount());
        assertNotNull(health.oldestPendingAgeSeconds());
        assertTrue(health.oldestPendingAgeSeconds() >= 39L);
        assertEquals(99L, health.cloudAckSequence());
    }

    @Test
    void shouldEnforceStoreOwnershipAndFailedStatusBeforeRetrying() {
        RetryFailedEdgeOutboxEventUseCase retryUseCase = new RetryFailedEdgeOutboxEventUseCase(outboxRepository, requeuePort);
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();

        EdgeOutboxEvent wrongStoreEvent = EdgeOutboxEvent.reconstitute(
                eventId, UUID.randomUUID(), "ORDER_UPSERT", aggregateId, "{}",
                EdgeOutboxStatus.FAILED, 1, null, Instant.now(), null, 5L, "erro");
        when(outboxRepository.findById(eventId)).thenReturn(Optional.of(wrongStoreEvent));
        assertThrows(ForbiddenAccessException.class, () -> retryUseCase.execute(STORE_ID, eventId));

        EdgeOutboxEvent pendingEvent = EdgeOutboxEvent.reconstitute(
                eventId, STORE_ID, "ORDER_UPSERT", aggregateId, "{}",
                EdgeOutboxStatus.PENDING, 0, null, Instant.now(), null, 5L, null);
        when(outboxRepository.findById(eventId)).thenReturn(Optional.of(pendingEvent));
        assertThrows(EdgeOutboxEventNotRetryableException.class, () -> retryUseCase.execute(STORE_ID, eventId));

        EdgeOutboxEvent failedEvent = EdgeOutboxEvent.reconstitute(
                eventId, STORE_ID, "ORDER_UPSERT", aggregateId, "{}",
                EdgeOutboxStatus.FAILED, 1, null, Instant.now(), null, 5L, "erro");
        when(outboxRepository.findById(eventId)).thenReturn(Optional.of(failedEvent));
        when(requeuePort.requeueByEventType(STORE_ID, "ORDER_UPSERT", aggregateId)).thenReturn(1);

        assertEquals(1, retryUseCase.execute(STORE_ID, eventId));
    }

    @Test
    void shouldValidateWindowAndThrowBusyWhenManualReconciliationSkipped() {
        TriggerEdgeReconciliationUseCase triggerUseCase = new TriggerEdgeReconciliationUseCase(reconcileEdgeDataUseCase);
        LocalDate from = LocalDate.of(2026, 11, 20);
        LocalDate to = LocalDate.of(2026, 11, 26);

        assertThrows(InvalidSyncRequestException.class,
                () -> triggerUseCase.execute(LocalDate.of(2026, 10, 1), to));

        when(reconcileEdgeDataUseCase.execute(ReconciliationTrigger.MANUAL, from, to)).thenReturn(Optional.empty());
        assertThrows(EdgeSyncBusyException.class, () -> triggerUseCase.execute(from, to));

        ReconciliationResult expected = new ReconciliationResult(UUID.randomUUID(), "CONSISTENT", 5, 0, 0, 0);
        when(reconcileEdgeDataUseCase.execute(ReconciliationTrigger.MANUAL, from, to)).thenReturn(Optional.of(expected));
        assertEquals(expected, triggerUseCase.execute(from, to));
    }
}


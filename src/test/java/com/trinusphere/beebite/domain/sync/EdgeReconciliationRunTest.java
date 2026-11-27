package com.trinusphere.beebite.domain.sync;

import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationRunStatus;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EdgeReconciliationRunTest {

    @Test
    void shouldStartAndFinishAsConsistentWhenNothingRequeued() {
        UUID storeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 11, 20);
        LocalDate to = LocalDate.of(2026, 11, 26);

        EdgeReconciliationRun run = EdgeReconciliationRun.start(storeId, ReconciliationTrigger.SCHEDULED, from, to);
        assertEquals(ReconciliationRunStatus.RUNNING, run.getStatus());

        run.recordComparison(14, 0);
        run.recordRepair(0, 0);
        Instant finished = Instant.now();
        run.finish(finished);

        assertEquals(ReconciliationRunStatus.CONSISTENT, run.getStatus());
        assertEquals(14, run.getBucketsCompared());
        assertEquals(0, run.getBucketsMismatched());
        assertEquals(finished, run.getFinishedAt());
    }

    @Test
    void shouldFinishAsRepairedWhenEventsAreRequeued() {
        EdgeReconciliationRun run = EdgeReconciliationRun.start(
                UUID.randomUUID(),
                ReconciliationTrigger.MANUAL,
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 11, 26)
        );

        run.recordComparison(6, 1);
        run.recordRepair(3, 1);
        run.finish(Instant.now());

        assertEquals(ReconciliationRunStatus.REPAIRED, run.getStatus());
        assertEquals(3, run.getRequeuedCount());
        assertEquals(1, run.getExtraInCloudCount());
    }

    @Test
    void shouldRecordErrorAndValidateDateWindow() {
        assertThrows(IllegalArgumentException.class, () -> EdgeReconciliationRun.start(
                UUID.randomUUID(),
                ReconciliationTrigger.MANUAL,
                LocalDate.of(2026, 11, 26),
                LocalDate.of(2026, 11, 20)
        ));

        EdgeReconciliationRun run = EdgeReconciliationRun.start(
                UUID.randomUUID(),
                ReconciliationTrigger.FULL_RESYNC,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 26)
        );
        Instant now = Instant.now();
        run.fail("Cloud unreachable", now);

        assertEquals(ReconciliationRunStatus.ERROR, run.getStatus());
        assertEquals("Cloud unreachable", run.getErrorMessage());
        assertEquals(now, run.getFinishedAt());
    }

    @Test
    void shouldMapReconciliationEntityTypesToOutboxEvents() {
        assertEquals("ORDER_UPSERT", ReconciliationEntityType.ORDER.getEventType());
        assertEquals("PAYMENT_UPSERT", ReconciliationEntityType.PAYMENT.getEventType());
        assertEquals("FINANCIAL_RECORD_UPSERT", ReconciliationEntityType.FINANCIAL_RECORD.getEventType());
        assertEquals("CASH_SESSION_UPSERT", ReconciliationEntityType.CASH_SESSION.getEventType());
        assertEquals("CASH_MOVEMENT_UPSERT", ReconciliationEntityType.CASH_MOVEMENT.getEventType());
        assertEquals("CASH_REGISTER_UPSERT", ReconciliationEntityType.CASH_REGISTER.getEventType());
    }
}


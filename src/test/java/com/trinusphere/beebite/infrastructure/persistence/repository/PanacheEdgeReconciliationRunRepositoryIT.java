package com.trinusphere.beebite.infrastructure.persistence.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;
import com.trinusphere.beebite.domain.sync.repository.EdgeReconciliationRunRepository;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationRunStatus;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class PanacheEdgeReconciliationRunRepositoryIT {

    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EdgeReconciliationRunRepository repository;

    @Test
    void save_shouldInsertAndUpdateReconciliationRun() {
        EdgeReconciliationRun run = EdgeReconciliationRun.start(
                DEFAULT_STORE_ID,
                ReconciliationTrigger.MANUAL,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 26)
        );
        EdgeReconciliationRun saved = repository.save(run);
        assertNotNull(saved.getId());
        assertEquals(ReconciliationRunStatus.RUNNING, saved.getStatus());

        saved.recordComparison(10, 2);
        saved.recordRepair(3, 0);
        saved.finish(Instant.now());
        EdgeReconciliationRun updated = repository.save(saved);

        assertEquals(ReconciliationRunStatus.REPAIRED, updated.getStatus());
        assertEquals(updated.getId(), repository.findLatestByStoreId(DEFAULT_STORE_ID).orElseThrow().getId());
    }
}


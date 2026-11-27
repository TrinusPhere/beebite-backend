package com.trinusphere.beebite.infrastructure.persistence.repository;

import com.trinusphere.beebite.domain.sync.repository.EdgeSyncedEventLogRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestTransaction
class PanacheEdgeSyncedEventLogRepositoryIT {

    @Inject
    EdgeSyncedEventLogRepository repository;

    @Test
    void purgeReceivedBefore_shouldExecuteCleanly() {
        int deleted = repository.purgeReceivedBefore(Instant.now().minusSeconds(86400 * 90L));
        assertTrue(deleted >= 0);
    }
}


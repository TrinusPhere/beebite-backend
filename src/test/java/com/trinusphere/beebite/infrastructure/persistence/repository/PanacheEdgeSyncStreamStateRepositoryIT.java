package com.trinusphere.beebite.infrastructure.persistence.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class PanacheEdgeSyncStreamStateRepositoryIT {

    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EdgeSyncStreamStateRepository repository;

    @Test
    void save_shouldInsertAndUpdateStreamState() {
        EdgeSyncStreamState state = repository.findOrInitialize(DEFAULT_STORE_ID);
        state.recordFailure("Timeout", Instant.now(), Duration.ofSeconds(15));
        EdgeSyncStreamState saved = repository.save(state);
        assertEquals(1, saved.getConsecutiveFailures());

        saved.recordSuccess(42L, Instant.now());
        EdgeSyncStreamState updated = repository.save(saved);
        assertEquals(0, updated.getConsecutiveFailures());
        assertEquals(42L, updated.getCloudAckSequence());
    }
}


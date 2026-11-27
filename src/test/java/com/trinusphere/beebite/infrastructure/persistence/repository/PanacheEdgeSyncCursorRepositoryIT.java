package com.trinusphere.beebite.infrastructure.persistence.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class PanacheEdgeSyncCursorRepositoryIT {

    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EdgeSyncCursorRepository repository;

    @Test
    void save_shouldInsertAndUpdateCursor() {
        EdgeSyncCursor initial = EdgeSyncCursor.initialize(DEFAULT_STORE_ID, 5L);
        EdgeSyncCursor saved = repository.save(initial);
        assertEquals(5L, saved.getLastContiguousSequence());

        saved.advanceTo(6L);
        EdgeSyncCursor updated = repository.save(saved);
        assertEquals(6L, updated.getLastContiguousSequence());
        assertEquals(6L, repository.findByStoreId(DEFAULT_STORE_ID).orElseThrow().getLastContiguousSequence());
    }
}


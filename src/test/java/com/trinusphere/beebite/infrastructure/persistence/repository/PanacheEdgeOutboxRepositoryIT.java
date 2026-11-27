package com.trinusphere.beebite.infrastructure.persistence.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class PanacheEdgeOutboxRepositoryIT {

    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EdgeOutboxRepository repository;

    @Test
    void save_shouldAssignMonotonicSequencesAndUpdateExistingEntity() {
        EdgeOutboxEvent ev1 = repository.save(new EdgeOutboxEvent(DEFAULT_STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"n\":1}"));
        EdgeOutboxEvent ev2 = repository.save(new EdgeOutboxEvent(DEFAULT_STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"n\":2}"));
        EdgeOutboxEvent ev3 = repository.save(new EdgeOutboxEvent(DEFAULT_STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"n\":3}"));

        assertNotNull(ev1.getId());
        assertNotNull(ev1.getSequence());
        assertEquals(ev1.getSequence() + 1L, ev2.getSequence());
        assertEquals(ev2.getSequence() + 1L, ev3.getSequence());

        ev1.markRejected("Payload inválido");
        EdgeOutboxEvent updated = repository.save(ev1);
        assertEquals(EdgeOutboxStatus.FAILED, updated.getStatus());
        assertEquals("Payload inválido", updated.getRejectedReason());
        assertEquals(ev1.getSequence(), updated.getSequence());
    }

    @Test
    void shouldReopenSyncedAfterAndPurgeOnlySynced() {
        EdgeOutboxEvent ev1 = repository.save(new EdgeOutboxEvent(DEFAULT_STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"n\":1}"));
        EdgeOutboxEvent ev2 = repository.save(new EdgeOutboxEvent(DEFAULT_STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{\"n\":2}"));
        EdgeOutboxEvent ev3 = repository.save(new EdgeOutboxEvent(DEFAULT_STORE_ID, "FOO_UPSERT", UUID.randomUUID(), "{}"));

        Instant oldSyncedAt = Instant.now().minusSeconds(86400 * 40L);
        repository.markSynced(List.of(ev1.getId(), ev2.getId()), oldSyncedAt);
        repository.markRejected(ev3.getId(), "Tipo não suportado");

        int reopened = repository.reopenSyncedAfter(DEFAULT_STORE_ID, ev1.getSequence());
        assertEquals(1, reopened);
        assertEquals(EdgeOutboxStatus.PENDING, repository.findById(ev2.getId()).orElseThrow().getStatus());
        assertEquals(EdgeOutboxStatus.SYNCED, repository.findById(ev1.getId()).orElseThrow().getStatus());

        int purged = repository.purgeSyncedBefore(Instant.now().minusSeconds(86400 * 30L));
        assertTrue(purged >= 1);
        assertTrue(repository.findById(ev1.getId()).isEmpty());
        assertTrue(repository.findById(ev2.getId()).isPresent());
        assertTrue(repository.findById(ev3.getId()).isPresent());
    }
}


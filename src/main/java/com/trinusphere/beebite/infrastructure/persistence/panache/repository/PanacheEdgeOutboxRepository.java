package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeOutboxEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.EdgeOutboxMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeOutboxRepository implements EdgeOutboxRepository {

    @Inject
    EdgeOutboxPanacheRepo outboxRepo;

    @Override
    @Transactional
    public EdgeOutboxEvent save(EdgeOutboxEvent event) {
        EdgeOutboxEntity entity = EdgeOutboxMapper.toEntity(event);
        if (entity.id == null) {
            if (entity.sequence == null) {
                Number nextSeq = (Number) outboxRepo.getEntityManager()
                        .createNativeQuery("""
                                INSERT INTO edge_sync_sequence (store_id, last_value)
                                VALUES (:storeId, 1)
                                ON CONFLICT (store_id) DO UPDATE
                                SET last_value = edge_sync_sequence.last_value + 1
                                RETURNING last_value
                                """)
                        .setParameter("storeId", entity.storeId)
                        .getSingleResult();
                entity.sequence = nextSeq.longValue();
            }
            outboxRepo.persist(entity);
        } else {
            if (entity.sequence == null) {
                EdgeOutboxEntity existing = outboxRepo.findById(entity.id);
                if (existing != null) {
                    entity.sequence = existing.sequence;
                }
            }
            entity = outboxRepo.getEntityManager().merge(entity);
        }
        return EdgeOutboxMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public List<UUID> findStoreIdsWithPending() {
        return outboxRepo.getEntityManager()
                .createQuery("select distinct e.storeId from EdgeOutboxEntity e where e.status = 'PENDING'", UUID.class)
                .getResultList();
    }

    @Override
    @Transactional
    public List<EdgeOutboxEvent> findPendingBatch(UUID storeId, int limit) {
        return outboxRepo.find("storeId = ?1 and status = 'PENDING' order by sequence asc", storeId)
                .page(Page.ofSize(limit))
                .list()
                .stream()
                .map(EdgeOutboxMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void markSynced(List<UUID> eventIds, Instant syncedAt) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        outboxRepo.update(
                "status = 'SYNCED', syncedAt = ?1, lastError = null, rejectedReason = null where id in ?2",
                syncedAt,
                eventIds
        );
    }

    @Override
    @Transactional
    public void recordTransientFailure(List<UUID> eventIds, String errorMessage) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        outboxRepo.update(
                "attempts = attempts + 1, lastError = ?1 where id in ?2 and status = 'PENDING'",
                errorMessage,
                eventIds
        );
    }

    @Override
    @Transactional
    public void markRejected(UUID eventId, String reason) {
        if (eventId == null) {
            return;
        }
        String safeReason = (reason == null || reason.isBlank()) ? "Rejeitado pela nuvem" : reason.trim();
        outboxRepo.update(
                "status = 'FAILED', rejectedReason = ?1, syncedAt = null, attempts = attempts + 1 where id = ?2",
                safeReason,
                eventId
        );
    }

    @Override
    @Transactional
    public int reopenSyncedAfter(UUID storeId, long lastContiguousSequence) {
        return outboxRepo.update(
                "status = 'PENDING', syncedAt = null where storeId = ?1 and status = 'SYNCED' and sequence > ?2",
                storeId,
                lastContiguousSequence
        );
    }

    @Override
    @Transactional
    public Optional<Long> findMinRetainedSequence(UUID storeId) {
        Long min = outboxRepo.getEntityManager()
                .createQuery("select min(e.sequence) from EdgeOutboxEntity e where e.storeId = :storeId", Long.class)
                .setParameter("storeId", storeId)
                .getSingleResult();
        return Optional.ofNullable(min);
    }

    @Override
    @Transactional
    public int purgeSyncedBefore(Instant cutoff) {
        return (int) outboxRepo.delete("status = 'SYNCED' and syncedAt is not null and syncedAt < ?1", cutoff);
    }

    @Override
    @Transactional
    public long countByStatus(UUID storeId, EdgeOutboxStatus status) {
        return outboxRepo.count("storeId = ?1 and status = ?2", storeId, status.name());
    }

    @Override
    @Transactional
    public Optional<Instant> findOldestPendingCreatedAt(UUID storeId) {
        Instant min = outboxRepo.getEntityManager()
                .createQuery(
                        "select min(e.createdAt) from EdgeOutboxEntity e where e.storeId = :storeId and e.status = 'PENDING'",
                        Instant.class)
                .setParameter("storeId", storeId)
                .getSingleResult();
        return Optional.ofNullable(min);
    }

    @Override
    @Transactional
    public Optional<EdgeOutboxEvent> findById(UUID id) {
        return outboxRepo.findByIdOptional(id).map(EdgeOutboxMapper::toDomain);
    }

    @Override
    @Transactional
    public List<EdgeOutboxEvent> findFailed(UUID storeId, int page, int size) {
        return outboxRepo.find("storeId = ?1 and status = 'FAILED' order by createdAt desc", storeId)
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(EdgeOutboxMapper::toDomain)
                .toList();
    }
}

package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeOutboxEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncedEventLogEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.EdgeOutboxMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeOutboxRepository implements EdgeOutboxRepository {

    @Inject
    EdgeOutboxPanacheRepo outboxRepo;
    @Inject
    EdgeSyncedEventLogPanacheRepo logRepo;

    @Override
    @Transactional
    public EdgeOutboxEvent save(EdgeOutboxEvent event) {
        EdgeOutboxEntity entity = EdgeOutboxMapper.toEntity(event);
        if (entity.id == null) {
            outboxRepo.persist(entity);
        } else {
            entity = outboxRepo.getEntityManager().merge(entity);
        }
        return EdgeOutboxMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public List<EdgeOutboxEvent> findPendingBatch(int limit) {
        return outboxRepo.find("status = 'PENDING' order by createdAt asc")
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
        outboxRepo.update("status = 'SYNCED', syncedAt = ?1, lastError = null where id in ?2", syncedAt, eventIds);
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxRepo.findByIdOptional(eventId).ifPresent(entity -> {
            entity.attempts = entity.attempts + 1;
            entity.lastError = errorMessage;
            outboxRepo.getEntityManager().merge(entity);
        });
    }

    @Override
    @Transactional
    public boolean isEventAlreadyProcessed(UUID eventId) {
        return logRepo.findByIdOptional(eventId).isPresent();
    }

    @Override
    @Transactional
    public void recordProcessedEvent(UUID eventId, UUID storeId, String eventType, UUID aggregateId) {
        EdgeSyncedEventLogEntity log = new EdgeSyncedEventLogEntity();
        log.eventId = eventId;
        log.storeId = storeId;
        log.eventType = eventType;
        log.aggregateId = aggregateId;
        log.receivedAt = Instant.now();
        logRepo.persist(log);
    }
}

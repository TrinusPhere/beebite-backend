package com.fidecode.biteflow.domain.sync.repository;

import com.fidecode.biteflow.domain.sync.entity.EdgeOutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EdgeOutboxRepository {
    EdgeOutboxEvent save(EdgeOutboxEvent event);

    List<EdgeOutboxEvent> findPendingBatch(int limit);

    void markSynced(List<UUID> eventIds, Instant syncedAt);

    void markFailed(UUID eventId, String errorMessage);

    boolean isEventAlreadyProcessed(UUID eventId);

    void recordProcessedEvent(UUID eventId, UUID storeId, String eventType, UUID aggregateId);
}

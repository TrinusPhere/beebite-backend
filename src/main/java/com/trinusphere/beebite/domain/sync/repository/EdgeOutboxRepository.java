package com.trinusphere.beebite.domain.sync.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EdgeOutboxRepository {
    EdgeOutboxEvent save(EdgeOutboxEvent event);

    List<UUID> findStoreIdsWithPending();

    List<EdgeOutboxEvent> findPendingBatch(UUID storeId, int limit);

    void markSynced(List<UUID> eventIds, Instant syncedAt);

    void recordTransientFailure(List<UUID> eventIds, String errorMessage);

    void markRejected(UUID eventId, String reason);

    int reopenSyncedAfter(UUID storeId, long lastContiguousSequence);

    Optional<Long> findMinRetainedSequence(UUID storeId);

    int purgeSyncedBefore(Instant cutoff);

    long countByStatus(UUID storeId, EdgeOutboxStatus status);

    Optional<Instant> findOldestPendingCreatedAt(UUID storeId);

    Optional<EdgeOutboxEvent> findById(UUID id);

    List<EdgeOutboxEvent> findFailed(UUID storeId, int page, int size);
}

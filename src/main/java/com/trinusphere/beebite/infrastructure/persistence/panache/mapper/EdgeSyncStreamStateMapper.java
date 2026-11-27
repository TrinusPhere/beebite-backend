package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncStreamStateEntity;

import java.time.Instant;

public final class EdgeSyncStreamStateMapper {

    private EdgeSyncStreamStateMapper() {
    }

    public static EdgeSyncStreamState toDomain(EdgeSyncStreamStateEntity entity) {
        if (entity == null) {
            return null;
        }
        return EdgeSyncStreamState.reconstitute(
                entity.storeId,
                entity.cloudAckSequence,
                entity.consecutiveFailures,
                entity.nextAttemptAt,
                entity.lastSuccessAt,
                entity.lastError,
                entity.fullResyncRequested
        );
    }

    public static EdgeSyncStreamStateEntity toEntity(EdgeSyncStreamState domain) {
        if (domain == null) {
            return null;
        }
        EdgeSyncStreamStateEntity entity = new EdgeSyncStreamStateEntity();
        entity.storeId = domain.getStoreId();
        entity.cloudAckSequence = domain.getCloudAckSequence();
        entity.consecutiveFailures = domain.getConsecutiveFailures();
        entity.nextAttemptAt = domain.getNextAttemptAt();
        entity.lastSuccessAt = domain.getLastSuccessAt();
        entity.lastError = domain.getLastError();
        entity.fullResyncRequested = domain.isFullResyncRequested();
        entity.updatedAt = Instant.now();
        return entity;
    }
}


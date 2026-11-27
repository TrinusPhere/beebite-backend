package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncCursorEntity;

import java.time.Instant;

public final class EdgeSyncCursorMapper {

    private EdgeSyncCursorMapper() {
    }

    public static EdgeSyncCursor toDomain(EdgeSyncCursorEntity entity) {
        if (entity == null) {
            return null;
        }
        return EdgeSyncCursor.reconstitute(entity.storeId, entity.lastContiguousSequence);
    }

    public static EdgeSyncCursorEntity toEntity(EdgeSyncCursor domain, Instant existingInitializedAt) {
        if (domain == null) {
            return null;
        }
        Instant now = Instant.now();
        EdgeSyncCursorEntity entity = new EdgeSyncCursorEntity();
        entity.storeId = domain.getStoreId();
        entity.lastContiguousSequence = domain.getLastContiguousSequence();
        entity.initializedAt = existingInitializedAt != null ? existingInitializedAt : now;
        entity.updatedAt = now;
        return entity;
    }
}


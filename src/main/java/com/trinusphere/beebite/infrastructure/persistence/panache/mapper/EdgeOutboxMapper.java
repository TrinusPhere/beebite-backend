package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeOutboxEntity;

public final class EdgeOutboxMapper {

    private EdgeOutboxMapper() {
    }

    public static EdgeOutboxEvent toDomain(EdgeOutboxEntity entity) {
        if (entity == null) {
            return null;
        }
        return EdgeOutboxEvent.reconstitute(
                entity.id,
                entity.storeId,
                entity.eventType,
                entity.aggregateId,
                entity.payload,
                entity.status != null ? EdgeOutboxStatus.valueOf(entity.status) : EdgeOutboxStatus.PENDING,
                entity.attempts,
                entity.lastError,
                entity.createdAt,
                entity.syncedAt
        );
    }

    public static EdgeOutboxEntity toEntity(EdgeOutboxEvent domain) {
        if (domain == null) {
            return null;
        }
        EdgeOutboxEntity entity = new EdgeOutboxEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.eventType = domain.getEventType();
        entity.aggregateId = domain.getAggregateId();
        entity.payload = domain.getPayloadJson();
        entity.status = domain.getStatus().name();
        entity.attempts = domain.getAttempts();
        entity.lastError = domain.getLastError();
        entity.createdAt = domain.getCreatedAt();
        entity.syncedAt = domain.getSyncedAt();
        return entity;
    }
}

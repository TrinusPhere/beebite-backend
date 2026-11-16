package com.trinusphere.beebite.domain.sync.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;

import java.time.Instant;
import java.util.UUID;

public class EdgeOutboxEvent implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private final String eventType;
    private final UUID aggregateId;
    private final String payloadJson;
    private EdgeOutboxStatus status;
    private int attempts;
    private String lastError;
    private final Instant createdAt;
    private Instant syncedAt;

    public EdgeOutboxEvent(UUID storeId, String eventType, UUID aggregateId, String payloadJson) {
        validate(storeId, eventType, aggregateId, payloadJson);
        this.storeId = storeId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payloadJson = payloadJson;
        this.status = EdgeOutboxStatus.PENDING;
        this.attempts = 0;
        this.createdAt = Instant.now();
    }

    private EdgeOutboxEvent(UUID id, UUID storeId, String eventType, UUID aggregateId, String payloadJson,
            EdgeOutboxStatus status, int attempts, String lastError, Instant createdAt, Instant syncedAt) {
        validate(storeId, eventType, aggregateId, payloadJson);
        this.id = id;
        this.storeId = storeId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payloadJson = payloadJson;
        this.status = status != null ? status : EdgeOutboxStatus.PENDING;
        this.attempts = attempts;
        this.lastError = lastError;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.syncedAt = syncedAt;
    }

    public static EdgeOutboxEvent reconstitute(UUID id, UUID storeId, String eventType, UUID aggregateId,
            String payloadJson, EdgeOutboxStatus status, int attempts, String lastError, Instant createdAt,
            Instant syncedAt) {
        return new EdgeOutboxEvent(id, storeId, eventType, aggregateId, payloadJson, status, attempts, lastError,
                createdAt, syncedAt);
    }

    private static void validate(UUID storeId, String eventType, UUID aggregateId, String payloadJson) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório.");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType é obrigatório.");
        }
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId é obrigatório.");
        }
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new IllegalArgumentException("payloadJson é obrigatório.");
        }
    }

    public void markSynced(Instant syncedAt) {
        this.status = EdgeOutboxStatus.SYNCED;
        this.syncedAt = syncedAt != null ? syncedAt : Instant.now();
        this.lastError = null;
    }

    public void recordAttemptFailure(String errorMessage) {
        this.attempts++;
        this.lastError = errorMessage;
        this.status = EdgeOutboxStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public EdgeOutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}

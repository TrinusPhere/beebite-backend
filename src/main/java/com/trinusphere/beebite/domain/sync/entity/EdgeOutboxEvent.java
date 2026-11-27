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
    private Long sequence;
    private String rejectedReason;

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
            EdgeOutboxStatus status, int attempts, String lastError, Instant createdAt, Instant syncedAt,
            Long sequence, String rejectedReason) {
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
        this.sequence = sequence;
        this.rejectedReason = rejectedReason;
    }

    public static EdgeOutboxEvent reconstitute(UUID id, UUID storeId, String eventType, UUID aggregateId,
            String payloadJson, EdgeOutboxStatus status, int attempts, String lastError, Instant createdAt,
            Instant syncedAt) {
        return new EdgeOutboxEvent(id, storeId, eventType, aggregateId, payloadJson, status, attempts, lastError,
                createdAt, syncedAt, null, null);
    }

    public static EdgeOutboxEvent reconstitute(UUID id, UUID storeId, String eventType, UUID aggregateId,
            String payloadJson, EdgeOutboxStatus status, int attempts, String lastError, Instant createdAt,
            Instant syncedAt, Long sequence, String rejectedReason) {
        return new EdgeOutboxEvent(id, storeId, eventType, aggregateId, payloadJson, status, attempts, lastError,
                createdAt, syncedAt, sequence, rejectedReason);
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
        this.rejectedReason = null;
    }

    public void markRejected(String reason) {
        this.status = EdgeOutboxStatus.FAILED;
        this.rejectedReason = (reason == null || reason.isBlank()) ? "Rejeitado pela nuvem" : reason.trim();
        this.syncedAt = null;
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

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }
}

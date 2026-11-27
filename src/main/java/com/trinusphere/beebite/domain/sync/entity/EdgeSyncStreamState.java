package com.trinusphere.beebite.domain.sync.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class EdgeSyncStreamState implements StoreGuard.StoreScoped {
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(5);

    private final UUID storeId;
    private long cloudAckSequence;
    private int consecutiveFailures;
    private Instant nextAttemptAt;
    private Instant lastSuccessAt;
    private String lastError;
    private boolean fullResyncRequested;

    private EdgeSyncStreamState(
            UUID storeId,
            long cloudAckSequence,
            int consecutiveFailures,
            Instant nextAttemptAt,
            Instant lastSuccessAt,
            String lastError,
            boolean fullResyncRequested) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório.");
        }
        this.storeId = storeId;
        this.cloudAckSequence = Math.max(0L, cloudAckSequence);
        this.consecutiveFailures = Math.max(0, consecutiveFailures);
        this.nextAttemptAt = nextAttemptAt;
        this.lastSuccessAt = lastSuccessAt;
        this.lastError = lastError;
        this.fullResyncRequested = fullResyncRequested;
    }

    public static EdgeSyncStreamState initial(UUID storeId) {
        return new EdgeSyncStreamState(storeId, 0L, 0, null, null, null, false);
    }

    public static EdgeSyncStreamState reconstitute(
            UUID storeId,
            long cloudAckSequence,
            int consecutiveFailures,
            Instant nextAttemptAt,
            Instant lastSuccessAt,
            String lastError,
            boolean fullResyncRequested) {
        return new EdgeSyncStreamState(
                storeId,
                cloudAckSequence,
                consecutiveFailures,
                nextAttemptAt,
                lastSuccessAt,
                lastError,
                fullResyncRequested
        );
    }

    public boolean canAttempt(Instant now) {
        Instant ref = now != null ? now : Instant.now();
        return nextAttemptAt == null || !ref.isBefore(nextAttemptAt);
    }

    public void recordSuccess(Long ackSequence, Instant now) {
        if (ackSequence != null) {
            this.cloudAckSequence = Math.max(0L, ackSequence);
        }
        this.consecutiveFailures = 0;
        this.nextAttemptAt = null;
        this.lastError = null;
        this.lastSuccessAt = now != null ? now : Instant.now();
    }

    public void recordFailure(String error, Instant now, Duration baseInterval) {
        Instant ref = now != null ? now : Instant.now();
        Duration base = (baseInterval == null || baseInterval.isNegative() || baseInterval.isZero())
                ? Duration.ofSeconds(15)
                : baseInterval;
        this.consecutiveFailures++;
        long factor = 1L << Math.min(consecutiveFailures, 10);
        Duration delay = base.multipliedBy(factor);
        Duration capped = delay.compareTo(MAX_BACKOFF) > 0 ? MAX_BACKOFF : delay;
        this.nextAttemptAt = ref.plus(capped);
        this.lastError = error;
    }

    public void requestFullResync() {
        this.fullResyncRequested = true;
    }

    public void clearFullResync() {
        this.fullResyncRequested = false;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public long getCloudAckSequence() {
        return cloudAckSequence;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isFullResyncRequested() {
        return fullResyncRequested;
    }
}


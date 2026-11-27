package com.trinusphere.beebite.domain.sync.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationRunStatus;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class EdgeReconciliationRun implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private final ReconciliationTrigger trigger;
    private final LocalDate windowFrom;
    private final LocalDate windowTo;
    private ReconciliationRunStatus status;
    private int bucketsCompared;
    private int bucketsMismatched;
    private int requeuedCount;
    private int extraInCloudCount;
    private String errorMessage;
    private final Instant startedAt;
    private Instant finishedAt;

    private EdgeReconciliationRun(
            UUID id,
            UUID storeId,
            ReconciliationTrigger trigger,
            LocalDate windowFrom,
            LocalDate windowTo,
            ReconciliationRunStatus status,
            int bucketsCompared,
            int bucketsMismatched,
            int requeuedCount,
            int extraInCloudCount,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório.");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("trigger é obrigatório.");
        }
        if (windowFrom == null || windowTo == null || windowFrom.isAfter(windowTo)) {
            throw new IllegalArgumentException("Janela de reconciliação inválida: windowFrom deve ser <= windowTo.");
        }
        this.id = id;
        this.storeId = storeId;
        this.trigger = trigger;
        this.windowFrom = windowFrom;
        this.windowTo = windowTo;
        this.status = status != null ? status : ReconciliationRunStatus.RUNNING;
        this.bucketsCompared = Math.max(0, bucketsCompared);
        this.bucketsMismatched = Math.max(0, bucketsMismatched);
        this.requeuedCount = Math.max(0, requeuedCount);
        this.extraInCloudCount = Math.max(0, extraInCloudCount);
        this.errorMessage = errorMessage;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.finishedAt = finishedAt;
    }

    public static EdgeReconciliationRun start(
            UUID storeId,
            ReconciliationTrigger trigger,
            LocalDate windowFrom,
            LocalDate windowTo) {
        return new EdgeReconciliationRun(
                null, storeId, trigger, windowFrom, windowTo,
                ReconciliationRunStatus.RUNNING, 0, 0, 0, 0, null, Instant.now(), null
        );
    }

    public static EdgeReconciliationRun reconstitute(
            UUID id,
            UUID storeId,
            ReconciliationTrigger trigger,
            LocalDate windowFrom,
            LocalDate windowTo,
            ReconciliationRunStatus status,
            int bucketsCompared,
            int bucketsMismatched,
            int requeuedCount,
            int extraInCloudCount,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt) {
        return new EdgeReconciliationRun(
                id, storeId, trigger, windowFrom, windowTo,
                status, bucketsCompared, bucketsMismatched, requeuedCount, extraInCloudCount,
                errorMessage, startedAt, finishedAt
        );
    }

    public void recordComparison(int compared, int mismatched) {
        this.bucketsCompared = Math.max(0, compared);
        this.bucketsMismatched = Math.max(0, mismatched);
    }

    public void recordRepair(int requeued, int extraInCloud) {
        this.requeuedCount = Math.max(0, requeued);
        this.extraInCloudCount = Math.max(0, extraInCloud);
    }

    public void finish(Instant now) {
        this.status = this.requeuedCount > 0 ? ReconciliationRunStatus.REPAIRED : ReconciliationRunStatus.CONSISTENT;
        this.finishedAt = now != null ? now : Instant.now();
        this.errorMessage = null;
    }

    public void fail(String error, Instant now) {
        this.status = ReconciliationRunStatus.ERROR;
        this.errorMessage = error;
        this.finishedAt = now != null ? now : Instant.now();
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

    public ReconciliationTrigger getTrigger() {
        return trigger;
    }

    public LocalDate getWindowFrom() {
        return windowFrom;
    }

    public LocalDate getWindowTo() {
        return windowTo;
    }

    public ReconciliationRunStatus getStatus() {
        return status;
    }

    public int getBucketsCompared() {
        return bucketsCompared;
    }

    public int getBucketsMismatched() {
        return bucketsMismatched;
    }

    public int getRequeuedCount() {
        return requeuedCount;
    }

    public int getExtraInCloudCount() {
        return extraInCloudCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}


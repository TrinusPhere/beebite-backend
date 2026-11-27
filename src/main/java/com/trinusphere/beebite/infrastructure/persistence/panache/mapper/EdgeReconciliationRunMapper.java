package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationRunStatus;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeReconciliationRunEntity;

public final class EdgeReconciliationRunMapper {

    private EdgeReconciliationRunMapper() {
    }

    public static EdgeReconciliationRun toDomain(EdgeReconciliationRunEntity entity) {
        if (entity == null) {
            return null;
        }
        return EdgeReconciliationRun.reconstitute(
                entity.id,
                entity.storeId,
                ReconciliationTrigger.valueOf(entity.trigger),
                entity.windowFrom,
                entity.windowTo,
                ReconciliationRunStatus.valueOf(entity.status),
                entity.bucketsCompared,
                entity.bucketsMismatched,
                entity.requeuedCount,
                entity.extraInCloudCount,
                entity.errorMessage,
                entity.startedAt,
                entity.finishedAt
        );
    }

    public static EdgeReconciliationRunEntity toEntity(EdgeReconciliationRun domain) {
        if (domain == null) {
            return null;
        }
        EdgeReconciliationRunEntity entity = new EdgeReconciliationRunEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.trigger = domain.getTrigger().name();
        entity.windowFrom = domain.getWindowFrom();
        entity.windowTo = domain.getWindowTo();
        entity.status = domain.getStatus().name();
        entity.bucketsCompared = domain.getBucketsCompared();
        entity.bucketsMismatched = domain.getBucketsMismatched();
        entity.requeuedCount = domain.getRequeuedCount();
        entity.extraInCloudCount = domain.getExtraInCloudCount();
        entity.errorMessage = domain.getErrorMessage();
        entity.startedAt = domain.getStartedAt();
        entity.finishedAt = domain.getFinishedAt();
        return entity;
    }
}


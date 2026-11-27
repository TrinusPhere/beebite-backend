package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeOutboxFailedEventResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationResult;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;

public final class EdgeSyncDtoMapper {

    private EdgeSyncDtoMapper() {
    }

    public static EdgeOutboxFailedEventResponse toFailedResponse(EdgeOutboxEvent event) {
        return new EdgeOutboxFailedEventResponse(
                event.getId(),
                event.getSequence(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAttempts(),
                event.getRejectedReason(),
                event.getCreatedAt()
        );
    }

    public static ReconciliationResult toReconciliationResult(EdgeReconciliationRun run) {
        return new ReconciliationResult(
                run.getId(),
                run.getStatus().name(),
                run.getBucketsCompared(),
                run.getBucketsMismatched(),
                run.getRequeuedCount(),
                run.getExtraInCloudCount()
        );
    }
}


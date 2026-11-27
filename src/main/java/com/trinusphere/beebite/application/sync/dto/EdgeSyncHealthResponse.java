package com.trinusphere.beebite.application.sync.dto;

import java.time.Instant;

public record EdgeSyncHealthResponse(
        String syncMode,
        long pendingCount,
        long failedCount,
        Long oldestPendingAgeSeconds,
        long cloudAckSequence,
        Instant lastSuccessAt,
        Instant nextAttemptAt,
        int consecutiveFailures,
        String lastError,
        boolean fullResyncRequested,
        ReconciliationResult lastReconciliation
) {
}


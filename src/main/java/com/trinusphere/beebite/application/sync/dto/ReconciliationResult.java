package com.trinusphere.beebite.application.sync.dto;

import java.util.UUID;

public record ReconciliationResult(
        UUID runId,
        String status,
        int bucketsCompared,
        int bucketsMismatched,
        int requeuedCount,
        int extraInCloudCount
) {
}


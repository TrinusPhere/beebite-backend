package com.trinusphere.beebite.application.sync.dto;

import java.time.Instant;
import java.util.List;

public record EdgeReconcileBucketsResponse(
        List<ReconciliationBucketDigest> mismatched,
        Instant serverTimestamp
) {
}


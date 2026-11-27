package com.trinusphere.beebite.application.sync.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EdgeReconcileBucketsRequest(
        UUID storeId,
        LocalDate from,
        LocalDate to,
        List<ReconciliationBucketDigest> digests
) {
}


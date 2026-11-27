package com.trinusphere.beebite.application.sync.dto;

import java.time.LocalDate;

public record ReconciliationBucketDigest(
        String entityType,
        LocalDate bucketDate,
        long count,
        String hash
) {
}


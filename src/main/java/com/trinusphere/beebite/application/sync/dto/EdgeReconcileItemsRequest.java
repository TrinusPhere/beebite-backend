package com.trinusphere.beebite.application.sync.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EdgeReconcileItemsRequest(
        UUID storeId,
        String entityType,
        LocalDate bucketDate,
        List<ReconciliationItemFingerprint> items
) {
}


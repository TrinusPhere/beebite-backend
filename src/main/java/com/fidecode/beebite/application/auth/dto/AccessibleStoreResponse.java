package com.fidecode.beebite.application.auth.dto;

import java.time.Instant;
import java.util.List;

public record AccessibleStoreResponse(
        String storeId,
        String storeName,
        String role,
        List<String> permissions,
        String syncMode,
        Instant lastEdgeSyncAt,
        boolean activeContext
) {
}

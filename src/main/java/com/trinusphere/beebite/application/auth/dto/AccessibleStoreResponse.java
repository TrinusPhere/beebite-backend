package com.trinusphere.beebite.application.auth.dto;

import java.time.Instant;
import java.util.List;

public record AccessibleStoreResponse(
        String storeId,
        String storeName,
        String tenantId,
        String role,
        List<String> permissions,
        String syncMode,
        Instant lastEdgeSyncAt,
        boolean activeContext,
        boolean sandboxBeta,
        boolean storeActive
) {
    public AccessibleStoreResponse(
            String storeId,
            String storeName,
            String role,
            List<String> permissions,
            String syncMode,
            Instant lastEdgeSyncAt,
            boolean activeContext) {
        this(storeId, storeName, null, role, permissions, syncMode, lastEdgeSyncAt, activeContext, false, true);
    }
}

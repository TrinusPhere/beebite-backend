package com.trinusphere.beebite.application.sync.dto;

public record InternalStoreCatalogContextResponse(
        String storeId,
        String tenantId,
        String catalogId,
        String storeName,
        String syncMode,
        boolean active
) {
}


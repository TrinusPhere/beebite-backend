package com.trinusphere.beebite.application.platform.dto;

public record CatalogSummaryDto(
        String id,
        String tenantId,
        String name,
        boolean defaultCatalog,
        int linkedStoresCount
) {
}


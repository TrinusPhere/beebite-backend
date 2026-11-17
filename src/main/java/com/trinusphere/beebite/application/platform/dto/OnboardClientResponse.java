package com.trinusphere.beebite.application.platform.dto;

import java.util.List;

public record OnboardClientResponse(
        String tenantId,
        String tradeName,
        String ownerUserId,
        String ownerLogin,
        String contractId,
        List<ProvisionedStoreDto> stores
) {
    public record ProvisionedStoreDto(
            String storeId,
            String storeName,
            String cnpj,
            String syncMode,
            String generatedEdgeDeviceToken
    ) {
    }
}


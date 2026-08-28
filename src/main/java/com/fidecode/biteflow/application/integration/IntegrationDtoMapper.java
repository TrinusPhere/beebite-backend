package com.fidecode.biteflow.application.integration;

import com.fidecode.biteflow.application.integration.dto.IntegrationResponse;
import com.fidecode.biteflow.domain.integration.entity.IntegrationConfig;

public final class IntegrationDtoMapper {
    private IntegrationDtoMapper() {
    }

    public static IntegrationResponse toResponse(IntegrationConfig c) {
        return new IntegrationResponse(c.getStatus(), c.getApiProvider(), c.getPhoneNumber(), c.getBusinessAccountId(),
                c.getClientId(), c.getExternalStoreId(), c.getStoreStatus(), has(c.getClientSecret()),
                has(c.getAccessToken()));
    }

    private static boolean has(String v) {
        return v != null && !v.isBlank();
    }
}

package com.fidecode.beebite.application.integration.dto;

public record IntegrationResponse(String status, String provider, String phoneNumber, String businessAccountId,
        String clientId, String merchantId, String storeStatus, boolean secretConfigured, boolean tokenConfigured) {
}

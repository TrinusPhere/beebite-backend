package com.fidecode.beebite.application.integration.dto;

public record WhatsappIntegrationRequest(String provider, String phoneNumber, String businessAccountId,
        String accessToken, String status) {
}

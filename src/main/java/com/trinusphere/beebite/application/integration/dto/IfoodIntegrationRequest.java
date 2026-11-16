package com.trinusphere.beebite.application.integration.dto;

public record IfoodIntegrationRequest(String clientId, String clientSecret, String merchantId, String status) {
}

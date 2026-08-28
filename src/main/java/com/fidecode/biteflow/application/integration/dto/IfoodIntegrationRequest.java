package com.fidecode.biteflow.application.integration.dto;

public record IfoodIntegrationRequest(String clientId, String clientSecret, String merchantId, String status) {
}

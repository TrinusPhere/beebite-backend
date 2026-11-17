package com.trinusphere.beebite.application.platform.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SaasInvoiceResponse(
        String id,
        String contractId,
        String tenantId,
        String tenantTradeName,
        String referenceMonth,
        String description,
        BigDecimal amount,
        LocalDate dueDate,
        Instant paidAt,
        String paymentMethod,
        String status,
        String notes,
        Instant createdAt
) {
}


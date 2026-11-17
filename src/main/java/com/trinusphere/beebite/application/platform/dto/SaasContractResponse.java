package com.trinusphere.beebite.application.platform.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SaasContractResponse(
        String id,
        String tenantId,
        String tenantTradeName,
        String planName,
        String billingCycle,
        BigDecimal monthlyFee,
        BigDecimal setupFee,
        int maxStores,
        boolean includesIfood,
        boolean includesEdgeSync,
        int billingDay,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String contractNotes,
        Instant createdAt
) {
}


package com.trinusphere.beebite.application.platform.dto;

import java.math.BigDecimal;

public record PlatformOverviewResponse(
        int totalTenants,
        int activeTenants,
        int suspendedTenants,
        int totalStores,
        int cloudStores,
        int edgeStores,
        BigDecimal mrrAmount,
        BigDecimal receivedThisMonth,
        BigDecimal pendingThisMonth,
        BigDecimal overdueAmount,
        int overdueInvoicesCount
) {
}


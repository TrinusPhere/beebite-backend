package com.trinusphere.beebite.application.platform.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TenantAccountResponse(
        String id,
        String companyName,
        String tradeName,
        String ownerName,
        String documentNumber,
        String email,
        String phone,
        String status,
        String notes,
        int storesCount,
        BigDecimal activeMonthlyFee,
        List<StoreSummaryDto> stores,
        Instant createdAt
) {
    public record StoreSummaryDto(
            String id,
            String name,
            String cnpj,
            String syncMode,
            boolean active,
            String edgeDeviceToken,
            Instant lastEdgeSyncAt
    ) {
    }
}


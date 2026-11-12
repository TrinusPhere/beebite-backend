package com.fidecode.beebite.application.kds.dto;

import java.time.LocalDateTime;

public record ProductionTaskResponse(
        String id,
        String orderId,
        String orderItemId,
        String sectorId,
        String displayOrderId,
        String orderType,
        String orderChannel,
        String customerName,
        String itemName,
        int quantity,
        String notes,
        String modifiersSummary,
        String comboName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime canceledAt
) {}


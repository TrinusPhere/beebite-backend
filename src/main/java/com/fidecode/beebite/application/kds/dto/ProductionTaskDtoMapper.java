package com.fidecode.beebite.application.kds.dto;

import com.fidecode.beebite.domain.kds.entity.ProductionTask;

public final class ProductionTaskDtoMapper {

    private ProductionTaskDtoMapper() {}

    public static ProductionTaskResponse toResponse(ProductionTask t) {
        if (t == null) return null;
        return new ProductionTaskResponse(
                t.getId() != null ? t.getId().toString() : null,
                t.getOrderId() != null ? t.getOrderId().toString() : null,
                t.getOrderItemId() != null ? t.getOrderItemId().toString() : null,
                t.getSectorId() != null ? t.getSectorId().toString() : null,
                t.getDisplayOrderId(),
                t.getOrderType(),
                t.getOrderChannel(),
                t.getCustomerName(),
                t.getItemName(),
                t.getQuantity(),
                t.getNotes(),
                t.getModifiersSummary(),
                t.getComboName(),
                t.getStatus().name(),
                t.getCreatedAt(),
                t.getStartedAt(),
                t.getCompletedAt(),
                t.getCanceledAt()
        );
    }
}


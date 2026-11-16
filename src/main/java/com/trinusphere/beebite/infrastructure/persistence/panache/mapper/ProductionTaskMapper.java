package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.kds.entity.ProductionTask;
import com.trinusphere.beebite.domain.kds.valueobject.TaskStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductionTaskEntity;

public final class ProductionTaskMapper {

    private ProductionTaskMapper() {}

    public static ProductionTask toDomain(ProductionTaskEntity entity) {
        if (entity == null) return null;
        TaskStatus status = entity.status != null ? TaskStatus.valueOf(entity.status) : TaskStatus.PENDING;
        return ProductionTask.reconstitute(
                entity.id,
                entity.storeId,
                entity.orderId,
                entity.orderItemId,
                entity.sectorId,
                entity.displayOrderId,
                entity.orderType,
                entity.orderChannel,
                entity.customerName,
                entity.itemName,
                entity.quantity,
                entity.notes,
                entity.modifiersSummary,
                entity.comboName,
                status,
                entity.createdAt,
                entity.startedAt,
                entity.completedAt,
                entity.canceledAt,
                entity.deletedAt
        );
    }

    public static ProductionTaskEntity toEntity(ProductionTask domain) {
        if (domain == null) return null;
        ProductionTaskEntity entity = new ProductionTaskEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.orderId = domain.getOrderId();
        entity.orderItemId = domain.getOrderItemId();
        entity.sectorId = domain.getSectorId();
        entity.displayOrderId = domain.getDisplayOrderId();
        entity.orderType = domain.getOrderType();
        entity.orderChannel = domain.getOrderChannel();
        entity.customerName = domain.getCustomerName();
        entity.itemName = domain.getItemName();
        entity.quantity = domain.getQuantity();
        entity.notes = domain.getNotes();
        entity.modifiersSummary = domain.getModifiersSummary();
        entity.comboName = domain.getComboName();
        entity.status = domain.getStatus() != null ? domain.getStatus().name() : TaskStatus.PENDING.name();
        entity.createdAt = domain.getCreatedAt();
        entity.startedAt = domain.getStartedAt();
        entity.completedAt = domain.getCompletedAt();
        entity.canceledAt = domain.getCanceledAt();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }
}

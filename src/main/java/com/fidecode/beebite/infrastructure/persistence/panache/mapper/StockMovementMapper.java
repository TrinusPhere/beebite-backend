package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.inventory.entity.StockMovement;
import com.fidecode.beebite.domain.inventory.valueobject.StockMovementType;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.StockMovementEntity;

public final class StockMovementMapper {
    private StockMovementMapper() {
    }

    public static StockMovement toDomain(StockMovementEntity e) {
        return StockMovement.reconstitute(e.id, e.storeId, e.ingredientId, StockMovementType.valueOf(e.type),
                e.quantity, e.previousStock, e.resultingStock, e.reason, e.document, e.sourceType, e.sourceId,
                e.createdBy, e.createdAt);
    }

    public static StockMovementEntity toEntity(StockMovement m) {
        var e = new StockMovementEntity();
        e.id = m.getId();
        e.storeId = m.getStoreId();
        e.ingredientId = m.getIngredientId();
        e.type = m.getType().name();
        e.quantity = m.getQuantity();
        e.previousStock = m.getPreviousStock();
        e.resultingStock = m.getResultingStock();
        e.reason = m.getReason();
        e.document = m.getDocument();
        e.sourceType = m.getSourceType();
        e.sourceId = m.getSourceId();
        e.createdBy = m.getCreatedBy();
        e.createdAt = m.getCreatedAt();
        return e;
    }
}

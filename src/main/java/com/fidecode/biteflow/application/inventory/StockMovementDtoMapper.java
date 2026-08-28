package com.fidecode.biteflow.application.inventory;

import com.fidecode.biteflow.application.inventory.dto.StockMovementResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.inventory.entity.StockMovement;
import java.time.format.DateTimeFormatter;

public final class StockMovementDtoMapper {
    private StockMovementDtoMapper() {
    }

    public static StockMovementResponse toResponse(StockMovement m, Ingredient i) {
        return new StockMovementResponse(m.getId().toString(), m.getIngredientId().toString(),
                i != null ? i.getName() : "Ingrediente", i != null ? i.getUnit().name() : null, m.getType().name(),
                m.getQuantity(), m.getPreviousStock(), m.getResultingStock(), m.getReason(), m.getDocument(),
                m.getCreatedAt() != null ? m.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
    }
}

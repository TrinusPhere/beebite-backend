package com.trinusphere.beebite.application.inventory;

import com.trinusphere.beebite.application.inventory.dto.StockMovementResponse;
import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.inventory.entity.StockMovement;
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

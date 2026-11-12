package com.fidecode.beebite.application.financial.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response — CMV unitário de um produto via ficha técnica.
 */
public record ProductCmvResponse(UUID productId, String productName, BigDecimal sellingPrice, BigDecimal productionCost,
        BigDecimal contributionMargin, BigDecimal cmvPercentage, String alertLevel, List<IngredientCost> ingredients) {
    public record IngredientCost(String name, BigDecimal quantity, String unit, BigDecimal proportionalCost) {
    }
}

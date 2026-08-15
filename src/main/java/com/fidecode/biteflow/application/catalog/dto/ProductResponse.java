package com.fidecode.biteflow.application.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
    String id,
    String name,
    String category,
    String description,
    BigDecimal price,
    String imageUrl,
    int stock,
    boolean active,
    List<TechnicalSheetItemResponse> technicalSheet,
    List<RemovableItemResponse> removableItems,
    List<String> modifierGroupIds,
    BigDecimal totalCost,
    BigDecimal profit,
    Instant createdAt
) {
    public record TechnicalSheetItemResponse(String ingredientId, String name, String unit, BigDecimal cost, BigDecimal quantity) {}
    public record RemovableItemResponse(String ingredientId, String name) {}
}

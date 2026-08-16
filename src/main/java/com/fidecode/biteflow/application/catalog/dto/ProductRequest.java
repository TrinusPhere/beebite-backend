package com.fidecode.biteflow.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
    @NotBlank(message = "Nome é obrigatório") String name,
    String category,
    String description,
    BigDecimal price,
    String imageUrl,
    Integer stock,
    List<TechnicalSheetItemDto> technicalSheet,
    List<String> removableItemIds,
    List<String> modifierGroupIds
) {
    public record TechnicalSheetItemDto(String ingredientId, BigDecimal quantity) {}
}

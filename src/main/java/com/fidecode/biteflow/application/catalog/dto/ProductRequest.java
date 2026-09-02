package com.fidecode.biteflow.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(@NotBlank(message = "Nome é obrigatório") String name, String categoryId,
        String description, BigDecimal price, String imageUrl, Integer stock, String status, String externalCode,
        String catalogItemType, List<ChannelPriceDto> channelPrices, String productType,
        Boolean trackStock, String productionSectorId, String saleUnit, Boolean availableDigitalMenu,
        Boolean availablePdv, List<TechnicalSheetItemDto> technicalSheet, List<String> removableItemIds,
        List<String> modifierGroupIds) {

    /** Compatibilidade com o contrato anterior do cadastro de produto. */
    public ProductRequest(String name, String categoryId, String description, BigDecimal price, String imageUrl,
            Integer stock, List<TechnicalSheetItemDto> technicalSheet, List<String> removableItemIds,
            List<String> modifierGroupIds) {
        this(name, categoryId, description, price, imageUrl, stock, null, null, null, null, null, null, null, null, null, null,
                technicalSheet, removableItemIds, modifierGroupIds);
    }

    public record ChannelPriceDto(String channel, BigDecimal price) {
    }

    public record TechnicalSheetItemDto(String ingredientId, BigDecimal quantity) {
    }
}

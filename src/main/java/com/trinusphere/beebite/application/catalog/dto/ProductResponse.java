package com.trinusphere.beebite.application.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(String id, String name, String categoryId, String categoryName, String description,
        BigDecimal price, BigDecimal estimatedCost, String imageUrl, int stock, String status, String externalCode, String catalogItemType,
        List<ChannelPriceDto> channelPrices, String productType, boolean trackStock,
        String productionSectorId, String saleUnit, boolean availableDigitalMenu, boolean availablePdv,
        List<TechnicalSheetItemResponse> technicalSheet, List<RemovableItemResponse> removableItems,
        List<String> modifierGroupIds, BigDecimal totalCost, BigDecimal profit, Instant createdAt) {
    public record ChannelPriceDto(String channel, BigDecimal price) {
    }

    public record TechnicalSheetItemResponse(String ingredientId, String name, String unit, BigDecimal cost,
            BigDecimal quantity) {
    }

    public record RemovableItemResponse(String ingredientId, String name) {
    }
}

package com.trinusphere.beebite.application.sync.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EdgeCatalogDeltaResponse(
        UUID catalogId,
        String catalogName,
        Instant serverTimestamp,
        List<CategorySyncDto> categories,
        List<ModifierGroupSyncDto> modifierGroups,
        List<AdditionalSyncDto> additionals,
        List<IngredientSyncDto> ingredients,
        List<ProductSyncDto> products
) {
    public record CategorySyncDto(
            UUID id,
            String name,
            String status,
            int displayOrder,
            List<UUID> defaultModifierGroupIds,
            Instant updatedAt,
            LocalDateTime deletedAt
    ) {
    }

    public record ModifierGroupSyncDto(
            UUID id,
            String name,
            String type,
            String semanticType,
            boolean required,
            int minSelections,
            int maxSelections,
            String icon,
            List<ModifierOptionSyncDto> options,
            Instant updatedAt,
            LocalDateTime deletedAt
    ) {
    }

    public record ModifierOptionSyncDto(
            UUID id,
            String name,
            BigDecimal price,
            int sortOrder,
            UUID linkedProductId
    ) {
    }

    public record AdditionalSyncDto(
            UUID id,
            String name,
            BigDecimal price,
            String category,
            boolean active,
            Instant updatedAt,
            LocalDateTime deletedAt
    ) {
    }

    public record IngredientSyncDto(
            UUID id,
            String name,
            String unit,
            BigDecimal costPrice,
            boolean active,
            Instant updatedAt,
            LocalDateTime deletedAt
    ) {
    }

    public record ProductSyncDto(
            UUID id,
            String name,
            UUID categoryId,
            String description,
            BigDecimal price,
            BigDecimal estimatedCost,
            String imageUrl,
            String status,
            String externalCode,
            String catalogItemType,
            String productType,
            boolean trackStock,
            String defaultSectorCode,
            String saleUnit,
            boolean availableDigitalMenu,
            boolean availablePdv,
            List<ChannelPriceSyncDto> channelPrices,
            List<TechnicalSheetSyncDto> technicalSheet,
            List<UUID> removableItemIds,
            List<UUID> modifierGroupIds,
            Instant updatedAt,
            LocalDateTime deletedAt
    ) {
    }

    public record ChannelPriceSyncDto(
            String channel,
            BigDecimal price
    ) {
    }

    public record TechnicalSheetSyncDto(
            UUID ingredientId,
            BigDecimal quantity,
            BigDecimal costSnapshot
    ) {
    }
}


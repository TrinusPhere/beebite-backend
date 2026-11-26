package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductCategoryEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ProductCategoryMapper {
    private ProductCategoryMapper() {
    }

    public static ProductCategory toDomain(ProductCategoryEntity e) {
        return toDomain(e, null);
    }

    public static ProductCategory toDomain(ProductCategoryEntity e, UUID contextStoreId) {
        if (e == null)
            return null;

        List<UUID> modifierGroupIds = new ArrayList<>();
        if (e.defaultModifierGroupIds != null && !e.defaultModifierGroupIds.isBlank()) {
            modifierGroupIds = Arrays.stream(e.defaultModifierGroupIds.split(",")).map(String::trim)
                    .map(UUID::fromString).collect(Collectors.toList());
        }

        return ProductCategory.reconstituteForCatalog(
                e.id,
                e.catalogId,
                contextStoreId != null ? contextStoreId : e.catalogId,
                e.name,
                e.status != null ? ProductStatus.valueOf(e.status) : ProductStatus.AVAILABLE,
                e.displayOrder,
                modifierGroupIds,
                e.createdAt != null ? LocalDateTime.ofInstant(e.createdAt, ZoneId.systemDefault()) : null
        );
    }

    public static ProductCategoryEntity toEntity(ProductCategory p) {
        return toEntity(p, p != null ? p.getCatalogId() : null);
    }

    public static ProductCategoryEntity toEntity(ProductCategory p, UUID resolvedCatalogId) {
        if (p == null)
            return null;

        ProductCategoryEntity e = new ProductCategoryEntity();
        e.id = p.getId();
        e.catalogId = resolvedCatalogId != null ? resolvedCatalogId : p.getCatalogId();
        e.name = p.getName();
        e.status = p.getStatus().name();
        e.displayOrder = p.getDisplayOrder();

        if (p.getDefaultModifierGroupIds() != null && !p.getDefaultModifierGroupIds().isEmpty()) {
            e.defaultModifierGroupIds = p.getDefaultModifierGroupIds().stream().map(UUID::toString)
                    .collect(Collectors.joining(","));
        } else {
            e.defaultModifierGroupIds = null;
        }

        return e;
    }
}

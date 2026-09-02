package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductStatus;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductCategoryEntity;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.ArrayList;

public final class ProductCategoryMapper {
    private ProductCategoryMapper() {
    }

    public static ProductCategory toDomain(ProductCategoryEntity e) {
        if (e == null)
            return null;

        List<UUID> modifierGroupIds = new ArrayList<>();
        if (e.defaultModifierGroupIds != null && !e.defaultModifierGroupIds.isBlank()) {
            modifierGroupIds = Arrays.stream(e.defaultModifierGroupIds.split(",")).map(String::trim)
                    .map(UUID::fromString).collect(Collectors.toList());
        }

        return ProductCategory.reconstitute(e.id, e.storeId, e.name, 
                e.status != null ? ProductStatus.valueOf(e.status) : ProductStatus.AVAILABLE, 
                e.displayOrder, modifierGroupIds,
                e.createdAt != null ? java.time.LocalDateTime.ofInstant(e.createdAt, java.time.ZoneId.systemDefault())
                        : null);
    }

    public static ProductCategoryEntity toEntity(ProductCategory p) {
        if (p == null)
            return null;

        ProductCategoryEntity e = new ProductCategoryEntity();
        e.id = p.getId();
        e.storeId = p.getStoreId();
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

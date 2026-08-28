package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CategoryDtoMapper {
    private CategoryDtoMapper() {}
    public static CategoryResponse toResponse(ProductCategory category) {
        List<String> modifierGroupIds = null;
        if (category.getDefaultModifierGroupIds() != null) {
            modifierGroupIds = category.getDefaultModifierGroupIds().stream().map(UUID::toString).collect(Collectors.toList());
        }

        return new CategoryResponse(
                category.getId().toString(),
                category.getName(),
                category.isActive(),
                category.getDisplayOrder(),
                modifierGroupIds,
                category.getCreatedAt() != null ? category.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}

package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.application.catalog.dto.CategoryRequest;
import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CreateCategoryUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public CreateCategoryUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse execute(UUID storeId, CategoryRequest request) {
        List<UUID> modifierGroupIds = null;
        if (request.defaultModifierGroupIds() != null) {
            modifierGroupIds = request.defaultModifierGroupIds().stream().map(UUID::fromString).collect(Collectors.toList());
        }

        ProductCategory category = ProductCategory.create(
                storeId,
                request.name(),
                request.displayOrder() != null ? request.displayOrder() : 0,
                modifierGroupIds
        );

        ProductCategory saved = categoryRepository.save(category);
        return CategoryDtoMapper.toResponse(saved);
    }
}

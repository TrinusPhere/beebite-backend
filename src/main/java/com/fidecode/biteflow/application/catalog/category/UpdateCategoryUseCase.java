package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.application.catalog.dto.CategoryRequest;
import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import com.fidecode.biteflow.domain.catalog.exception.CategoryNotFoundException;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UpdateCategoryUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public UpdateCategoryUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse execute(UUID storeId, UUID id, CategoryRequest request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        StoreGuard.enforce(category, storeId);

        List<UUID> modifierGroupIds = null;
        if (request.defaultModifierGroupIds() != null) {
            modifierGroupIds = request.defaultModifierGroupIds().stream().map(UUID::fromString).collect(Collectors.toList());
        }

        category.update(
                request.name(),
                true, // assuming active by default for now
                request.displayOrder() != null ? request.displayOrder() : category.getDisplayOrder(),
                modifierGroupIds
        );

        return CategoryDtoMapper.toResponse(categoryRepository.save(category));
    }
}

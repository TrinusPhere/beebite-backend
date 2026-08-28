package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import com.fidecode.biteflow.domain.catalog.exception.CategoryNotFoundException;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetCategoryUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public GetCategoryUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse execute(UUID storeId, UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        StoreGuard.enforce(category, storeId);
        return CategoryDtoMapper.toResponse(category);
    }
}

package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteCategoryUseCase {

    private final ProductCategoryRepository categoryRepository;

    @Inject
    public DeleteCategoryUseCase(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void execute(UUID storeId, UUID id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        StoreGuard.enforce(category, storeId);
        categoryRepository.deleteById(id);
    }
}

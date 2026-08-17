package com.fidecode.biteflow.application.catalog.category;

import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BulkApplyCategoryModifiersUseCase {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Inject
    public BulkApplyCategoryModifiersUseCase(ProductCategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID storeId, UUID categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (!category.getStoreId().equals(storeId)) throw new ForbiddenAccessException("Acesso negado");
        
        if (category.getDefaultModifierGroupIds() == null) {
            return;
        }

        List<Product> products = productRepository.findByStoreIdAndCategoryId(category.getStoreId(), categoryId);
        for (Product p : products) {
            p.setModifierGroupIds(category.getDefaultModifierGroupIds());
            productRepository.save(p);
        }
    }
}

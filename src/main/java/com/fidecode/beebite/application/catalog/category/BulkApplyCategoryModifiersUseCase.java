package com.fidecode.beebite.application.catalog.category;

import com.fidecode.beebite.domain.catalog.entity.ProductCategory;
import com.fidecode.beebite.domain.catalog.entity.Product;
import com.fidecode.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.beebite.domain.catalog.repository.ProductRepository;
import com.fidecode.beebite.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fidecode.beebite.application.shared.port.TransactionExecutor;
import com.fidecode.beebite.domain.catalog.exception.CategoryNotFoundException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BulkApplyCategoryModifiersUseCase {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final TransactionExecutor tx;

    @Inject
    public BulkApplyCategoryModifiersUseCase(ProductCategoryRepository categoryRepository,
            ProductRepository productRepository, TransactionExecutor tx) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.tx = tx;
    }

    public void execute(UUID storeId, UUID categoryId) {
        tx.execute(() -> {
            ProductCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId.toString()));
            StoreGuard.enforce(category, storeId);

            if (category.getDefaultModifierGroupIds() == null) {
                return null;
            }

            List<Product> products = productRepository.findByStoreIdAndCategoryId(category.getStoreId(), categoryId);
            for (Product p : products) {
                p.replaceModifierGroups(category.getDefaultModifierGroupIds());
                productRepository.save(p);
            }
            return null;
        });
    }
}

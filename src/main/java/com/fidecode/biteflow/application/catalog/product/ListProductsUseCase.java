package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.application.shared.PageResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.shared.PageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListProductsUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductResponseAssembler productResponseAssembler;

    @Inject
    public ListProductsUseCase(ProductRepository productRepository, IngredientRepository ingredientRepository,
            ProductCategoryRepository productCategoryRepository, ProductResponseAssembler productResponseAssembler) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productResponseAssembler = productResponseAssembler;
    }

    public PageResponse<ProductResponse> execute(UUID storeId, String categoryId, boolean pendingCost, int page, int size) {
        PageResult<Product> productPage;
        if (pendingCost) {
            productPage = productRepository.findPendingCostByStoreId(storeId, page, size);
        } else if (categoryId != null && !categoryId.isBlank()) {
            productPage = productRepository.findByStoreIdAndCategoryId(storeId, UUID.fromString(categoryId), page, size);
        } else {
            productPage = productRepository.findAllByStoreId(storeId, page, size);
        }

        // Otimização contra N+1
        Map<UUID, Ingredient> ingredientCache = ingredientRepository.findAllByStoreId(storeId).stream()
                .collect(Collectors.toMap(Ingredient::getId, i -> i));

        Map<UUID, ProductCategory> categoryCache = productCategoryRepository.findAllByStoreId(storeId).stream()
                .collect(Collectors.toMap(ProductCategory::getId, c -> c));

        List<ProductResponse> content = productPage.getContent().stream()
                .map(p -> productResponseAssembler.toResponse(p, ingredientCache, categoryCache)).toList();

        return new PageResponse<>(content, productPage.getPage(), productPage.getSize(), productPage.getTotalElements(),
                productPage.getTotalPages());
    }
}

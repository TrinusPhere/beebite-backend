package com.fidecode.beebite.application.catalog.product;

import com.fidecode.beebite.application.catalog.dto.ProductResponse;
import com.fidecode.beebite.domain.catalog.entity.Ingredient;
import com.fidecode.beebite.domain.catalog.entity.Product;
import com.fidecode.beebite.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.beebite.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.IngredientRepository;
import com.fidecode.beebite.domain.catalog.repository.ProductRepository;
import com.fidecode.beebite.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetProductUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductResponseAssembler productResponseAssembler;

    @Inject
    public GetProductUseCase(ProductRepository productRepository, IngredientRepository ingredientRepository,
            ProductResponseAssembler productResponseAssembler) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productResponseAssembler = productResponseAssembler;
    }

    public ProductResponse execute(UUID storeId, UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id.toString()));
        StoreGuard.enforce(product, storeId);

        List<UUID> ingredientIds = new ArrayList<>();
        if (product.getTechnicalSheet() != null) {
            ingredientIds
                    .addAll(product.getTechnicalSheet().stream().map(TechnicalSheetItem::getIngredientId).toList());
        }
        if (product.getRemovableItemIds() != null) {
            ingredientIds.addAll(product.getRemovableItemIds());
        }
        Map<UUID, Ingredient> ingredientCache = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, i -> i));

        return productResponseAssembler.toResponse(product, ingredientCache, null);
    }
}

package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
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
    private final ProductDtoMapper productDtoMapper;

    @Inject
    public GetProductUseCase(ProductRepository productRepository, 
                             IngredientRepository ingredientRepository,
                             ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productDtoMapper = productDtoMapper;
    }

    public ProductResponse execute(UUID storeId, UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        if (!product.getStoreId().equals(storeId)) throw new ForbiddenAccessException("Acesso negado");
        
        List<UUID> ingredientIds = new ArrayList<>();
        if (product.getTechnicalSheet() != null) {
            ingredientIds.addAll(product.getTechnicalSheet().stream().map(TechnicalSheetItem::getIngredientId).toList());
        }
        if (product.getRemovableItemIds() != null) {
            ingredientIds.addAll(product.getRemovableItemIds());
        }
        Map<UUID, Ingredient> ingredientCache = ingredientRepository.findAllById(ingredientIds).stream()
            .collect(Collectors.toMap(Ingredient::getId, i -> i));
            
        return productDtoMapper.toResponse(product, ingredientCache, null);
    }
}

package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductRequest;
import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductDtoMapper productDtoMapper;

    @Inject
    public UpdateProductUseCase(ProductRepository productRepository, 
                                IngredientRepository ingredientRepository,
                                ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productDtoMapper = productDtoMapper;
    }

    public ProductResponse execute(UUID storeId, UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        StoreGuard.enforce(product, storeId);

        product.setName(request.name());
        UUID categoryId = request.categoryId() != null && !request.categoryId().isBlank() ? UUID.fromString(request.categoryId()) : null;
        product.setCategoryId(categoryId);
        product.setDescription(request.description());
        if (request.price() != null) product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        if (request.stock() != null) product.setStock(request.stock());

        List<UUID> ingredientIds = new ArrayList<>();
        if (request.technicalSheet() != null) {
            ingredientIds.addAll(request.technicalSheet().stream().map(i -> UUID.fromString(i.ingredientId())).toList());
        }
        if (request.removableItemIds() != null) {
            ingredientIds.addAll(request.removableItemIds().stream().map(UUID::fromString).toList());
        }

        Map<UUID, Ingredient> ingredientCache = ingredientRepository.findAllById(ingredientIds).stream()
            .collect(Collectors.toMap(Ingredient::getId, i -> i));

        if (request.technicalSheet() != null) {
            List<TechnicalSheetItem> sheet = new ArrayList<>();
            for (var item : request.technicalSheet()) {
                UUID ingId = UUID.fromString(item.ingredientId());
                Ingredient ing = ingredientCache.get(ingId);
                BigDecimal costSnapshot = ing != null ? ing.getCostPrice() : BigDecimal.ZERO;
                sheet.add(new TechnicalSheetItem(id, ingId, item.quantity(), costSnapshot));
            }
            product.setTechnicalSheet(sheet);
        }

        if (request.removableItemIds() != null) {
            product.setRemovableItemIds(request.removableItemIds().stream().map(UUID::fromString).toList());
        }
        if (request.modifierGroupIds() != null) {
            product.setModifierGroupIds(request.modifierGroupIds().stream().map(UUID::fromString).toList());
        }

        return productDtoMapper.toResponse(productRepository.save(product), ingredientCache, null);
    }
}

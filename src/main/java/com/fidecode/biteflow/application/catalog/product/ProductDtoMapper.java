package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ProductDtoMapper {

    private final IngredientRepository ingredientRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Inject
    public ProductDtoMapper(IngredientRepository ingredientRepository, ProductCategoryRepository productCategoryRepository) {
        this.ingredientRepository = ingredientRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    public ProductResponse toResponse(Product p) {
        return toResponse(p, null, null);
    }

    public ProductResponse toResponse(Product p, Map<UUID, Ingredient> ingredientCache, Map<UUID, ProductCategory> categoryCache) {
        List<ProductResponse.TechnicalSheetItemResponse> sheetResponse = new ArrayList<>();
        if (p.getTechnicalSheet() != null) {
            for (var item : p.getTechnicalSheet()) {
                Ingredient ing = ingredientCache != null 
                    ? ingredientCache.get(item.getIngredientId()) 
                    : ingredientRepository.findById(item.getIngredientId()).orElse(null);

                sheetResponse.add(new ProductResponse.TechnicalSheetItemResponse(
                    item.getIngredientId().toString(),
                    ing != null ? ing.getName() : "Desconhecido",
                    ing != null ? ing.getUnit().name() : "UN",
                    item.getCostSnapshot(),
                    item.getQuantity()
                ));
            }
        }

        List<ProductResponse.RemovableItemResponse> removableResponse = new ArrayList<>();
        if (p.getRemovableItemIds() != null) {
            for (UUID id : p.getRemovableItemIds()) {
                Ingredient ing = ingredientCache != null 
                    ? ingredientCache.get(id) 
                    : ingredientRepository.findById(id).orElse(null);

                removableResponse.add(new ProductResponse.RemovableItemResponse(
                    id.toString(), ing != null ? ing.getName() : "Desconhecido"
                ));
            }
        }

        List<String> modGroupIds = p.getModifierGroupIds() != null
            ? p.getModifierGroupIds().stream().map(UUID::toString).toList()
            : List.of();

        String categoryName = null;
        if (p.getCategoryId() != null) {
            if (categoryCache != null) {
                ProductCategory cat = categoryCache.get(p.getCategoryId());
                if (cat != null) categoryName = cat.getName();
            } else {
                categoryName = productCategoryRepository.findById(p.getCategoryId())
                    .map(ProductCategory::getName)
                    .orElse(null);
            }
        }

        return new ProductResponse(
            p.getId() != null ? p.getId().toString() : null,
            p.getName(), 
            p.getCategoryId() != null ? p.getCategoryId().toString() : null, 
            categoryName, 
            p.getDescription(),
            p.getPrice(), p.getImageUrl(), p.getStock(), p.isActive(),
            sheetResponse, removableResponse, modGroupIds,
            p.calculateTotalCost(), p.calculateProfit(), null
        );
    }
}

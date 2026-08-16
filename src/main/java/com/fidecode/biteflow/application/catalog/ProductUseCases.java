package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.ProductRequest;
import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.IngredientRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductUseCases {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Inject
    public ProductUseCases(ProductRepository productRepository, 
                           IngredientRepository ingredientRepository,
                           ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    public ProductResponse create(UUID storeId, ProductRequest request) {
        UUID categoryId = request.categoryId() != null && !request.categoryId().isBlank() ? UUID.fromString(request.categoryId()) : null;
        Product product = new Product(storeId, request.name(), categoryId, request.price());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        if (request.stock() != null) product.setStock(request.stock());

        // Build technical sheet with cost snapshots
        if (request.technicalSheet() != null) {
            List<TechnicalSheetItem> sheet = new ArrayList<>();
            for (var item : request.technicalSheet()) {
                UUID ingredientId = UUID.fromString(item.ingredientId());
                BigDecimal costSnapshot = ingredientRepository.findById(ingredientId)
                    .map(Ingredient::getCostPrice)
                    .orElse(BigDecimal.ZERO);
                sheet.add(new TechnicalSheetItem(null, ingredientId, item.quantity(), costSnapshot));
            }
            product.setTechnicalSheet(sheet);
        }

        if (request.removableItemIds() != null) {
            product.setRemovableItemIds(request.removableItemIds().stream().map(UUID::fromString).toList());
        }
        if (request.modifierGroupIds() != null) {
            product.setModifierGroupIds(request.modifierGroupIds().stream().map(UUID::fromString).toList());
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id.toString()));

        product.setName(request.name());
        UUID categoryId = request.categoryId() != null && !request.categoryId().isBlank() ? UUID.fromString(request.categoryId()) : null;
        product.setCategoryId(categoryId);
        product.setDescription(request.description());
        if (request.price() != null) product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        if (request.stock() != null) product.setStock(request.stock());

        if (request.technicalSheet() != null) {
            List<TechnicalSheetItem> sheet = new ArrayList<>();
            for (var item : request.technicalSheet()) {
                UUID ingredientId = UUID.fromString(item.ingredientId());
                BigDecimal costSnapshot = ingredientRepository.findById(ingredientId)
                    .map(Ingredient::getCostPrice)
                    .orElse(BigDecimal.ZERO);
                sheet.add(new TechnicalSheetItem(id, ingredientId, item.quantity(), costSnapshot));
            }
            product.setTechnicalSheet(sheet);
        }

        if (request.removableItemIds() != null) {
            product.setRemovableItemIds(request.removableItemIds().stream().map(UUID::fromString).toList());
        }
        if (request.modifierGroupIds() != null) {
            product.setModifierGroupIds(request.modifierGroupIds().stream().map(UUID::fromString).toList());
        }

        return toResponse(productRepository.save(product));
    }

    public void delete(UUID id) {
        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id.toString()));
        productRepository.deleteById(id);
    }

    public ProductResponse getById(UUID id) {
        return productRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ProductNotFoundException(id.toString()));
    }

    public List<ProductResponse> list(UUID storeId, String categoryId) {
        List<Product> products = (categoryId != null && !categoryId.isBlank())
            ? productRepository.findByStoreIdAndCategoryId(storeId, UUID.fromString(categoryId))
            : productRepository.findAllByStoreId(storeId);
        return products.stream().map(this::toResponse).toList();
    }

    public ProductResponse updateRemovableItems(UUID productId, List<String> ingredientIds) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        product.setRemovableItemIds(ingredientIds != null ? ingredientIds.stream().map(UUID::fromString).toList() : List.of());
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateModifierGroups(UUID productId, List<String> groupIds) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        product.setModifierGroupIds(groupIds != null ? groupIds.stream().map(UUID::fromString).toList() : List.of());
        return toResponse(productRepository.save(product));
    }

    private ProductResponse toResponse(Product p) {
        // Build technical sheet response with ingredient names
        List<ProductResponse.TechnicalSheetItemResponse> sheetResponse = new ArrayList<>();
        if (p.getTechnicalSheet() != null) {
            for (var item : p.getTechnicalSheet()) {
                Ingredient ing = ingredientRepository.findById(item.getIngredientId()).orElse(null);
                sheetResponse.add(new ProductResponse.TechnicalSheetItemResponse(
                    item.getIngredientId().toString(),
                    ing != null ? ing.getName() : "Desconhecido",
                    ing != null ? ing.getUnit().name() : "UN",
                    item.getCostSnapshot(),
                    item.getQuantity()
                ));
            }
        }

        // Build removable items response
        List<ProductResponse.RemovableItemResponse> removableResponse = new ArrayList<>();
        if (p.getRemovableItemIds() != null) {
            for (UUID id : p.getRemovableItemIds()) {
                Ingredient ing = ingredientRepository.findById(id).orElse(null);
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
            categoryName = productCategoryRepository.findById(p.getCategoryId())
                .map(ProductCategory::getName)
                .orElse(null);
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

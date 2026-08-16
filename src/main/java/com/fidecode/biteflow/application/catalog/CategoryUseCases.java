package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.CategoryRequest;
import com.fidecode.biteflow.application.catalog.dto.CategoryResponse;
import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.ZoneOffset;

@ApplicationScoped
public class CategoryUseCases {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Inject
    public CategoryUseCases(ProductCategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public CategoryResponse create(UUID storeId, CategoryRequest request) {
        List<UUID> modifierGroupIds = null;
        if (request.defaultModifierGroupIds() != null) {
            modifierGroupIds = request.defaultModifierGroupIds().stream().map(UUID::fromString).collect(Collectors.toList());
        }

        ProductCategory category = ProductCategory.create(
                storeId,
                request.name(),
                request.displayOrder() != null ? request.displayOrder() : 0,
                modifierGroupIds
        );

        ProductCategory saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public CategoryResponse update(UUID id, CategoryRequest request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        List<UUID> modifierGroupIds = null;
        if (request.defaultModifierGroupIds() != null) {
            modifierGroupIds = request.defaultModifierGroupIds().stream().map(UUID::fromString).collect(Collectors.toList());
        }

        category.update(
                request.name(),
                true, // assuming active by default for now
                request.displayOrder() != null ? request.displayOrder() : category.getDisplayOrder(),
                modifierGroupIds
        );

        return toResponse(categoryRepository.save(category));
    }

    public void delete(UUID id) {
        categoryRepository.deleteById(id);
    }

    public List<CategoryResponse> list(UUID storeId) {
        return categoryRepository.findAllByStoreId(storeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public void bulkApplyModifiers(UUID categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        if (category.getDefaultModifierGroupIds() == null) {
            return;
        }

        List<Product> products = productRepository.findByStoreIdAndCategoryId(category.getStoreId(), categoryId);
        for (Product p : products) {
            p.setModifierGroupIds(category.getDefaultModifierGroupIds());
            productRepository.save(p);
        }
    }

    private CategoryResponse toResponse(ProductCategory category) {
        List<String> modifierGroupIds = null;
        if (category.getDefaultModifierGroupIds() != null) {
            modifierGroupIds = category.getDefaultModifierGroupIds().stream().map(UUID::toString).collect(Collectors.toList());
        }

        return new CategoryResponse(
                category.getId().toString(),
                category.getName(),
                category.isActive(),
                category.getDisplayOrder(),
                modifierGroupIds,
                category.getCreatedAt() != null ? category.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}

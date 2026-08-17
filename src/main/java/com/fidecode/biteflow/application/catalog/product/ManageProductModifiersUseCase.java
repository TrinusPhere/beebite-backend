package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.exception.ProductNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ManageProductModifiersUseCase {

    private final ProductRepository productRepository;
    private final ProductDtoMapper productDtoMapper;

    @Inject
    public ManageProductModifiersUseCase(ProductRepository productRepository, ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.productDtoMapper = productDtoMapper;
    }

    public ProductResponse updateRemovableItems(UUID storeId, UUID productId, List<String> ingredientIds) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        if (!product.getStoreId().equals(storeId)) throw new ForbiddenAccessException("Acesso negado");
        product.setRemovableItemIds(ingredientIds != null ? ingredientIds.stream().map(UUID::fromString).toList() : List.of());
        return productDtoMapper.toResponse(productRepository.save(product)); 
    }

    public ProductResponse updateModifierGroups(UUID storeId, UUID productId, List<String> groupIds) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        if (!product.getStoreId().equals(storeId)) throw new ForbiddenAccessException("Acesso negado");
        product.setModifierGroupIds(groupIds != null ? groupIds.stream().map(UUID::fromString).toList() : List.of());
        return productDtoMapper.toResponse(productRepository.save(product));
    }
}

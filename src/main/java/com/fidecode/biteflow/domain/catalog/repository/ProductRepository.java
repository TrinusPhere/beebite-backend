package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    com.fidecode.biteflow.domain.shared.PageResult<Product> findAllByStoreId(UUID storeId, int page, int size);
    com.fidecode.biteflow.domain.shared.PageResult<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId, int page, int size);
    List<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId);
    List<Product> findAllByStoreId(UUID storeId);
    List<Product> findAllById(List<UUID> ids);
    Product save(Product product);
    void deleteById(UUID id);
}

package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    List<Product> findAllByStoreId(UUID storeId);
    List<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId);
    Product save(Product product);
    void deleteById(UUID id);
}

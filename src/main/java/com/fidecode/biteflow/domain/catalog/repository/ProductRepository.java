package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    List<Product> findAllByStoreId(UUID storeId);
    List<Product> findByStoreIdAndCategory(UUID storeId, String category);
    List<String> findDistinctCategoriesByStoreId(UUID storeId);
    Product save(Product product);
    void deleteById(UUID id);
}

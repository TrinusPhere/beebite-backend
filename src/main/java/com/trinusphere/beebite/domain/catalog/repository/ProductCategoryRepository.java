package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository {
    Optional<ProductCategory> findById(UUID id);

    List<ProductCategory> findAllByStoreId(UUID storeId);

    ProductCategory save(ProductCategory category);

    void deleteById(UUID id);
}

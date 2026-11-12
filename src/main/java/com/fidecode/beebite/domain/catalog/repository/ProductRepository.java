package com.fidecode.beebite.domain.catalog.repository;

import com.fidecode.beebite.domain.catalog.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);

    com.fidecode.beebite.domain.shared.PageResult<Product> findAllByStoreId(UUID storeId, int page, int size);

    com.fidecode.beebite.domain.shared.PageResult<Product> findPendingCostByStoreId(UUID storeId, int page, int size);

    com.fidecode.beebite.domain.shared.PageResult<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId,
            int page, int size);

    List<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId);

    List<Product> findAllByStoreId(UUID storeId);

    List<Product> findAllById(List<UUID> ids);

    Product save(Product product);

    void deleteById(UUID id);

    /**
     * Busca um produto pelo externalCode e storeId, INCLUINDO soft-deleted.
     * Usa SQL nativo para contornar o @SQLRestriction("deleted_at IS NULL").
     */
    Optional<Product> findByExternalCodeAndStoreId(String externalCode, UUID storeId);
}

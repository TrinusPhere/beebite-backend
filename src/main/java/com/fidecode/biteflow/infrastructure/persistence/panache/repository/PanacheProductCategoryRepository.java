package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.catalog.entity.ProductCategory;
import com.fidecode.biteflow.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductCategoryEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.ProductCategoryMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheProductCategoryRepository implements ProductCategoryRepository {

    @Inject
    ProductCategoryPanacheRepo repo;

    @Override
    public Optional<ProductCategory> findById(UUID id) {
        return repo.findByIdOptional(id).map(ProductCategoryMapper::toDomain);
    }

    @Override
    public List<ProductCategory> findAllByStoreId(UUID storeId) {
        return repo.list("storeId = ?1 ORDER BY displayOrder ASC, name ASC", storeId).stream()
                .map(ProductCategoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ProductCategory save(ProductCategory category) {
        ProductCategoryEntity entity = ProductCategoryMapper.toEntity(category);
        repo.persist(entity);
        return ProductCategoryMapper.toDomain(entity);
    }

    @Override
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            repo.getEntityManager().merge(entity);
        });
    }
}

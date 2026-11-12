package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fidecode.beebite.domain.catalog.entity.ProductCategory;
import com.fidecode.beebite.domain.catalog.repository.ProductCategoryRepository;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.ProductCategoryEntity;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.ProductCategoryMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
                .map(ProductCategoryMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductCategory save(ProductCategory category) {
        ProductCategoryEntity entity = ProductCategoryMapper.toEntity(category);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return ProductCategoryMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            repo.getEntityManager().merge(entity);
        });
    }
}

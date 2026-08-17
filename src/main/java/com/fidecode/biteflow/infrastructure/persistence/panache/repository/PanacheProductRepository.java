package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.repository.ProductRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.ProductMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheProductRepository implements ProductRepository {

    @Inject ProductPanacheRepo repo;
    @Inject EntityManager em;

    @Override
    @Transactional
    public Optional<Product> findById(UUID id) {
        return repo.findByIdOptional(id).map(ProductMapper::toDomain);
    }

    @Override
    public List<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId) {
        return repo.find("storeId = ?1 and categoryId = ?2", storeId, categoryId).list().stream()
            .map(ProductMapper::toDomain).toList();
    }

    @Override
    public List<Product> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(ProductMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Product> findAllByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Product> content = query.list().stream().map(ProductMapper::toDomain).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId, int page, int size) {
        var query = repo.find("storeId = ?1 and categoryId = ?2", storeId, categoryId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Product> content = query.list().stream().map(ProductMapper::toDomain).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(), query.pageCount());
    }

    @Override
    @Transactional
    public List<Product> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repo.find("id in ?1", ids).list().stream()
            .map(ProductMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Product save(Product product) {
        ProductEntity entity = ProductMapper.toEntity(product);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        return ProductMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            em.merge(entity);
        });
    }
}

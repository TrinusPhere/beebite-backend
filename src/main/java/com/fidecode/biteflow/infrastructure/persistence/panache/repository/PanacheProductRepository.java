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
        if (product.getId() == null) {
            ProductEntity entity = ProductMapper.toEntity(product);
            repo.persist(entity);
            return ProductMapper.toDomain(entity);
        }

        ProductEntity entity = repo.findById(product.getId());
        if (entity == null) {
            ProductEntity newEntity = ProductMapper.toEntity(product);
            return ProductMapper.toDomain(em.merge(newEntity));
        }

        entity.name = product.getName();
        entity.categoryId = product.getCategoryId();
        entity.description = product.getDescription();
        entity.price = product.getPrice();
        entity.imageUrl = product.getImageUrl();
        entity.stock = product.getStock();
        entity.active = product.isActive();

        entity.removableItemIds.clear();
        if (product.getRemovableItemIds() != null) {
            entity.removableItemIds.addAll(product.getRemovableItemIds());
        }

        entity.modifierGroupIds.clear();
        if (product.getModifierGroupIds() != null) {
            entity.modifierGroupIds.addAll(product.getModifierGroupIds());
        }

        if (product.getTechnicalSheet() == null) {
            entity.technicalSheet.clear();
        } else {
            entity.technicalSheet.removeIf(tse -> product.getTechnicalSheet().stream().noneMatch(i -> i.getIngredientId().equals(tse.ingredientId)));
            for (var item : product.getTechnicalSheet()) {
                var existing = entity.technicalSheet.stream().filter(tse -> tse.ingredientId.equals(item.getIngredientId())).findFirst();
                if (existing.isPresent()) {
                    existing.get().quantity = item.getQuantity();
                    existing.get().costSnapshot = item.getCostSnapshot();
                } else {
                    var tse = new com.fidecode.biteflow.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity();
                    tse.id = item.getId();
                    tse.product = entity;
                    tse.ingredientId = item.getIngredientId();
                    tse.quantity = item.getQuantity();
                    tse.costSnapshot = item.getCostSnapshot();
                    entity.technicalSheet.add(tse);
                }
            }
        }

        entity = em.merge(entity);
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

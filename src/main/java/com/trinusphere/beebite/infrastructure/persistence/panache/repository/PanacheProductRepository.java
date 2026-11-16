package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.ProductMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheProductRepository implements ProductRepository {

    @Inject
    ProductPanacheRepo repo;
    @Inject
    EntityManager em;

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
        return repo.find("storeId", storeId).list().stream().map(ProductMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public com.trinusphere.beebite.domain.shared.PageResult<Product> findAllByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Product> content = query.list().stream().map(ProductMapper::toDomain).toList();
        return new com.trinusphere.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public com.trinusphere.beebite.domain.shared.PageResult<Product> findPendingCostByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId = ?1 and estimatedCost is null and technicalSheet is empty", storeId)
                .page(io.quarkus.panache.common.Page.of(page, size));
        List<Product> content = query.list().stream().map(ProductMapper::toDomain).toList();
        return new com.trinusphere.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public com.trinusphere.beebite.domain.shared.PageResult<Product> findByStoreIdAndCategoryId(UUID storeId,
            UUID categoryId, int page, int size) {
        var query = repo.find("storeId = ?1 and categoryId = ?2", storeId, categoryId)
                .page(io.quarkus.panache.common.Page.of(page, size));
        List<Product> content = query.list().stream().map(ProductMapper::toDomain).toList();
        return new com.trinusphere.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public List<Product> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.find("id in ?1", ids).list().stream().map(ProductMapper::toDomain).toList();
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
        entity.estimatedCost = product.getEstimatedCost();
        entity.imageUrl = product.getImageUrl();
        entity.stock = product.getStock();
        entity.status = product.getStatus().name();
        entity.externalCode = product.getExternalCode();
        entity.catalogItemType = product.getCatalogItemType().name();
        entity.productType = product.getProductType().name();
        entity.trackStock = product.isTrackStock();
        entity.productionSectorId = product.getProductionSectorId();
        entity.saleUnit = product.getSaleUnit().name();
        entity.availableDigitalMenu = product.isAvailableDigitalMenu();
        entity.availablePdv = product.isAvailablePdv();

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
            entity.technicalSheet.removeIf(tse -> product.getTechnicalSheet().stream()
                    .noneMatch(i -> i.getIngredientId().equals(tse.ingredientId)));
            for (var item : product.getTechnicalSheet()) {
                var existing = entity.technicalSheet.stream()
                        .filter(tse -> tse.ingredientId.equals(item.getIngredientId())).findFirst();
                if (existing.isPresent()) {
                    existing.get().quantity = item.getQuantity();
                    existing.get().costSnapshot = item.getCostSnapshot();
                } else {
                    var tse = new TechnicalSheetItemEntity();
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

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Optional<Product> findByExternalCodeAndStoreId(String externalCode, UUID storeId) {
        // SQL nativo ignora o @SQLRestriction("deleted_at IS NULL") do Hibernate
        var results = (java.util.List<Object[]>) em.createNativeQuery(
                "SELECT id::text, deleted_at FROM product WHERE external_code = :code AND store_id = :storeId LIMIT 1")
                .setParameter("code", externalCode)
                .setParameter("storeId", storeId)
                .getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = results.get(0);
        UUID productId = UUID.fromString(row[0].toString());
        boolean isSoftDeleted = row[1] != null;

        // Se o produto estava soft-deleted, restaura para que o Hibernate consiga carregá-lo
        if (isSoftDeleted) {
            em.createNativeQuery("UPDATE product SET deleted_at = NULL WHERE id = :id")
                    .setParameter("id", productId)
                    .executeUpdate();
            em.flush();
            em.clear(); // Limpa o cache L1 para forçar reload
        }

        ProductEntity entity = em.find(ProductEntity.class, productId);
        return Optional.ofNullable(entity).map(ProductMapper::toDomain);
    }

}

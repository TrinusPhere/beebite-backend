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
    public Optional<Product> findById(UUID id) {
        return repo.findByIdOptional(id).map(this::toDomainFull);
    }

    @Override
    public List<Product> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(this::toDomainFull).toList();
    }

    @Override
    public List<Product> findByStoreIdAndCategory(UUID storeId, String category) {
        return repo.find("storeId = ?1 and category = ?2", storeId, category).list().stream()
            .map(this::toDomainFull).toList();
    }

    @Override
    public List<String> findDistinctCategoriesByStoreId(UUID storeId) {
        return em.createQuery("SELECT DISTINCT p.category FROM ProductEntity p WHERE p.storeId = :storeId AND p.category IS NOT NULL ORDER BY p.category", String.class)
            .setParameter("storeId", storeId)
            .getResultList();
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

        UUID productId = entity.id;

        // Save technical sheet
        em.createQuery("DELETE FROM TechnicalSheetItemEntity WHERE productId = :pid")
            .setParameter("pid", productId).executeUpdate();
        if (product.getTechnicalSheet() != null) {
            for (var item : product.getTechnicalSheet()) {
                TechnicalSheetItemEntity tse = new TechnicalSheetItemEntity();
                tse.productId = productId;
                tse.ingredientId = item.getIngredientId();
                tse.quantity = item.getQuantity();
                tse.costSnapshot = item.getCostSnapshot();
                em.persist(tse);
            }
        }

        // Save removable items
        em.createNativeQuery("DELETE FROM product_removable_item WHERE product_id = :pid")
            .setParameter("pid", productId).executeUpdate();
        if (product.getRemovableItemIds() != null) {
            for (UUID ingId : product.getRemovableItemIds()) {
                em.createNativeQuery("INSERT INTO product_removable_item (product_id, ingredient_id) VALUES (:pid, :iid)")
                    .setParameter("pid", productId).setParameter("iid", ingId).executeUpdate();
            }
        }

        // Save modifier groups
        em.createNativeQuery("DELETE FROM product_modifier_group WHERE product_id = :pid")
            .setParameter("pid", productId).executeUpdate();
        if (product.getModifierGroupIds() != null) {
            for (UUID gid : product.getModifierGroupIds()) {
                em.createNativeQuery("INSERT INTO product_modifier_group (product_id, modifier_group_id) VALUES (:pid, :gid)")
                    .setParameter("pid", productId).setParameter("gid", gid).executeUpdate();
            }
        }

        return toDomainFull(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        em.createNativeQuery("DELETE FROM product_removable_item WHERE product_id = :pid")
            .setParameter("pid", id).executeUpdate();
        em.createNativeQuery("DELETE FROM product_modifier_group WHERE product_id = :pid")
            .setParameter("pid", id).executeUpdate();
        em.createQuery("DELETE FROM TechnicalSheetItemEntity WHERE productId = :pid")
            .setParameter("pid", id).executeUpdate();
        repo.deleteById(id);
    }

    private Product toDomainFull(ProductEntity e) {
        List<TechnicalSheetItemEntity> sheetEntities = em
            .createQuery("SELECT t FROM TechnicalSheetItemEntity t WHERE t.productId = :pid", TechnicalSheetItemEntity.class)
            .setParameter("pid", e.id).getResultList();

        @SuppressWarnings("unchecked")
        List<UUID> removableIds = em
            .createNativeQuery("SELECT ingredient_id FROM product_removable_item WHERE product_id = :pid")
            .setParameter("pid", e.id).getResultList();

        @SuppressWarnings("unchecked")
        List<UUID> modGroupIds = em
            .createNativeQuery("SELECT modifier_group_id FROM product_modifier_group WHERE product_id = :pid")
            .setParameter("pid", e.id).getResultList();

        return ProductMapper.toDomain(e, sheetEntities, removableIds, modGroupIds);
    }
}

package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.catalog.entity.ProductComboComponent;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ProductComboComponentEntity;

public final class ProductComboComponentMapper {

    private ProductComboComponentMapper() {}

    public static ProductComboComponent toDomain(ProductComboComponentEntity entity) {
        if (entity == null) return null;
        return ProductComboComponent.reconstitute(
                entity.id,
                entity.storeId,
                entity.comboProductId,
                entity.componentProductId,
                entity.quantity,
                entity.createdAt,
                entity.deletedAt
        );
    }

    public static ProductComboComponentEntity toEntity(ProductComboComponent domain) {
        if (domain == null) return null;
        ProductComboComponentEntity entity = new ProductComboComponentEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.comboProductId = domain.getComboProductId();
        entity.componentProductId = domain.getComponentProductId();
        entity.quantity = domain.getQuantity();
        entity.createdAt = domain.getCreatedAt();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }
}

package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;

import java.util.List;
import java.util.UUID;

public final class ProductMapper {
    private ProductMapper() {}

    public static Product toDomain(ProductEntity e, List<TechnicalSheetItemEntity> sheetEntities,
                                   List<UUID> removableItemIds, List<UUID> modifierGroupIds) {
        if (e == null) return null;
        Product p = new Product(e.storeId, e.name, e.categoryId, e.price);
        p.setId(e.id);
        p.setDescription(e.description);
        p.setImageUrl(e.imageUrl);
        p.setStock(e.stock);
        p.setActive(e.active);

        if (sheetEntities != null) {
            p.setTechnicalSheet(sheetEntities.stream().map(se -> {
                TechnicalSheetItem item = new TechnicalSheetItem(se.productId, se.ingredientId, se.quantity, se.costSnapshot);
                item.setId(se.id);
                return item;
            }).toList());
        }

        p.setRemovableItemIds(removableItemIds);
        p.setModifierGroupIds(modifierGroupIds);
        return p;
    }

    public static ProductEntity toEntity(Product p) {
        ProductEntity e = new ProductEntity();
        e.id = p.getId();
        e.storeId = p.getStoreId();
        e.name = p.getName();
        e.categoryId = p.getCategoryId();
        e.description = p.getDescription();
        e.price = p.getPrice();
        e.imageUrl = p.getImageUrl();
        e.stock = p.getStock();
        e.active = p.isActive();
        return e;
    }
}

package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;

import java.util.List;
import java.util.UUID;

public final class ProductMapper {
    private ProductMapper() {}

    public static Product toDomain(ProductEntity e) {
        if (e == null) return null;
        Product p = new Product(e.storeId, e.name, e.categoryId, e.price);
        p.setId(e.id);
        p.setVersion(e.version); // P9 — round-trip do @Version
        p.setDescription(e.description);
        p.setImageUrl(e.imageUrl);
        p.setStock(e.stock);
        p.setActive(e.active);

        if (e.technicalSheet != null) {
            p.setTechnicalSheet(e.technicalSheet.stream().map(se -> {
                TechnicalSheetItem item = new TechnicalSheetItem(e.id, se.ingredientId, se.quantity, se.costSnapshot);
                item.setId(se.id);
                return item;
            }).toList());
        }

        p.setRemovableItemIds(e.removableItemIds);
        p.setModifierGroupIds(e.modifierGroupIds);
        return p;
    }

    public static ProductEntity toEntity(Product p) {
        ProductEntity e = new ProductEntity();
        e.id = p.getId();
        e.version = p.getVersion(); // P9 — round-trip do @Version
        e.storeId = p.getStoreId();
        e.name = p.getName();
        e.categoryId = p.getCategoryId();
        e.description = p.getDescription();
        e.price = p.getPrice();
        e.imageUrl = p.getImageUrl();
        e.stock = p.getStock();
        e.active = p.isActive();

        if (p.getTechnicalSheet() != null) {
            for (TechnicalSheetItem item : p.getTechnicalSheet()) {
                TechnicalSheetItemEntity tse = new TechnicalSheetItemEntity();
                tse.id = item.getId();
                tse.product = e;
                tse.ingredientId = item.getIngredientId();
                tse.quantity = item.getQuantity();
                tse.costSnapshot = item.getCostSnapshot();
                e.technicalSheet.add(tse);
            }
        }

        if (p.getRemovableItemIds() != null) {
            e.removableItemIds.addAll(p.getRemovableItemIds());
        }

        if (p.getModifierGroupIds() != null) {
            e.modifierGroupIds.addAll(p.getModifierGroupIds());
        }

        return e;
    }
}

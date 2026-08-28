package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductType;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;

public final class ProductMapper {
    private ProductMapper() {
    }

    public static Product toDomain(ProductEntity e) {
        if (e == null)
            return null;
        var sheet = e.technicalSheet != null ? e.technicalSheet.stream().map(se -> {
            TechnicalSheetItem item = new TechnicalSheetItem(e.id, se.ingredientId, se.quantity, se.costSnapshot);
            item.setId(se.id);
            return item;
        }).toList() : java.util.List.<TechnicalSheetItem> of();
        return Product.reconstitute(e.id, e.version, e.storeId, e.name, e.categoryId, e.description, e.price,
                e.imageUrl, e.stock, e.active,
                e.productType != null ? ProductType.valueOf(e.productType) : ProductType.PRODUCED, e.trackStock,
                e.productionSectorId, e.saleUnit != null ? Unit.valueOf(e.saleUnit) : Unit.UN, e.availableDigitalMenu,
                e.availablePdv, sheet, e.removableItemIds, e.modifierGroupIds);
    }

    public static ProductEntity toEntity(Product p) {
        ProductEntity e = new ProductEntity();
        e.id = p.getId();
        e.version = p.getVersion();
        e.storeId = p.getStoreId();
        e.name = p.getName();
        e.categoryId = p.getCategoryId();
        e.description = p.getDescription();
        e.price = p.getPrice();
        e.imageUrl = p.getImageUrl();
        e.stock = p.getStock();
        e.active = p.isActive();
        e.productType = p.getProductType().name();
        e.trackStock = p.isTrackStock();
        e.productionSectorId = p.getProductionSectorId();
        e.saleUnit = p.getSaleUnit().name();
        e.availableDigitalMenu = p.isAvailableDigitalMenu();
        e.availablePdv = p.isAvailablePdv();
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
        if (p.getRemovableItemIds() != null)
            e.removableItemIds.addAll(p.getRemovableItemIds());
        if (p.getModifierGroupIds() != null)
            e.modifierGroupIds.addAll(p.getModifierGroupIds());
        return e;
    }
}

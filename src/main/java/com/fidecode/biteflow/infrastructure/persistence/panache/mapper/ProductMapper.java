package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductType;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductStatus;
import com.fidecode.biteflow.domain.catalog.valueobject.CatalogItemType;
import com.fidecode.biteflow.domain.catalog.valueobject.ChannelPrice;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.TechnicalSheetItemEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ProductChannelPriceEntity;

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
        var prices = e.channelPrices != null ? e.channelPrices.stream()
                .map(pe -> new ChannelPrice(pe.channel, pe.price)).toList()
                : java.util.List.<ChannelPrice> of();
        return Product.reconstitute(e.id, e.version, e.storeId, e.name, e.categoryId, e.description, e.price,
                e.imageUrl, e.stock, 
                e.status != null ? ProductStatus.valueOf(e.status) : ProductStatus.AVAILABLE, 
                e.externalCode,
                e.catalogItemType != null ? CatalogItemType.valueOf(e.catalogItemType) : CatalogItemType.DEFAULT,
                prices,
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
        e.status = p.getStatus().name();
        e.externalCode = p.getExternalCode();
        e.catalogItemType = p.getCatalogItemType().name();
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
        if (p.getChannelPrices() != null) {
            for (ChannelPrice cp : p.getChannelPrices()) {
                ProductChannelPriceEntity cpe = new ProductChannelPriceEntity();
                cpe.product = e;
                cpe.channel = cp.getChannel();
                cpe.price = cp.getPrice();
                e.channelPrices.add(cpe);
            }
        }
        if (p.getRemovableItemIds() != null)
            e.removableItemIds.addAll(p.getRemovableItemIds());
        if (p.getModifierGroupIds() != null)
            e.modifierGroupIds.addAll(p.getModifierGroupIds());
        return e;
    }
}

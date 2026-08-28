package com.fidecode.biteflow.application.catalog.product;

import com.fidecode.biteflow.application.catalog.dto.ProductResponse;
import com.fidecode.biteflow.domain.catalog.entity.*;
import java.util.*;

public final class ProductDtoMapper {
    private ProductDtoMapper() {}
    public static ProductResponse toResponse(Product p, Map<UUID, Ingredient> ingredientCache, Map<UUID, ProductCategory> categoryCache) {
        List<ProductResponse.TechnicalSheetItemResponse> sheet = new ArrayList<>();
        if (p.getTechnicalSheet()!=null) for (var item:p.getTechnicalSheet()) { var ing=ingredientCache!=null?ingredientCache.get(item.getIngredientId()):null; sheet.add(new ProductResponse.TechnicalSheetItemResponse(item.getIngredientId().toString(),ing!=null?ing.getName():"Desconhecido",ing!=null?ing.getUnit().name():"UN",item.getCostSnapshot(),item.getQuantity())); }
        List<ProductResponse.RemovableItemResponse> removable = new ArrayList<>();
        if (p.getRemovableItemIds()!=null) for (UUID id:p.getRemovableItemIds()) { var ing=ingredientCache!=null?ingredientCache.get(id):null; removable.add(new ProductResponse.RemovableItemResponse(id.toString(),ing!=null?ing.getName():"Desconhecido")); }
        String categoryName=null; if(p.getCategoryId()!=null&&categoryCache!=null){var c=categoryCache.get(p.getCategoryId());if(c!=null)categoryName=c.getName();}
        var modIds=p.getModifierGroupIds()!=null?p.getModifierGroupIds().stream().map(UUID::toString).toList():List.<String>of();
        return new ProductResponse(p.getId()!=null?p.getId().toString():null,p.getName(),p.getCategoryId()!=null?p.getCategoryId().toString():null,categoryName,p.getDescription(),p.getPrice(),p.getImageUrl(),p.getStock(),p.isActive(),p.getProductType().name(),p.isTrackStock(),p.getProductionSectorId()!=null?p.getProductionSectorId().toString():null,p.getSaleUnit().name(),p.isAvailableDigitalMenu(),p.isAvailablePdv(),sheet,removable,modIds,p.calculateTotalCost(),p.calculateProfit(),null);
    }
}

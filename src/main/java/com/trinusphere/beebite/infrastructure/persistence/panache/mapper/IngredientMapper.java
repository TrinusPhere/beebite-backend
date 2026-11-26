package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.catalog.valueobject.Unit;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.IngredientEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.StoreIngredientStockEntity;

import java.math.BigDecimal;
import java.util.UUID;

public final class IngredientMapper {
    private IngredientMapper() {
    }

    public static Ingredient toDomain(IngredientEntity e) {
        return toDomain(e, null, null);
    }

    public static Ingredient toDomain(IngredientEntity e, UUID contextStoreId, StoreIngredientStockEntity stockEntity) {
        if (e == null)
            return null;
        BigDecimal stock = (stockEntity != null && stockEntity.stock != null) ? stockEntity.stock : BigDecimal.ZERO;
        BigDecimal minStock = (stockEntity != null && stockEntity.minStock != null) ? stockEntity.minStock : BigDecimal.ZERO;
        return Ingredient.reconstituteForCatalog(
                e.id,
                e.catalogId,
                contextStoreId != null ? contextStoreId : e.catalogId,
                e.name,
                stock,
                e.unit != null ? Unit.valueOf(e.unit) : Unit.UN,
                e.costPrice,
                minStock,
                e.active
        );
    }

    public static IngredientEntity toEntity(Ingredient i) {
        return toEntity(i, i != null ? i.getCatalogId() : null);
    }

    public static IngredientEntity toEntity(Ingredient i, UUID resolvedCatalogId) {
        IngredientEntity e = new IngredientEntity();
        e.id = i.getId();
        e.catalogId = resolvedCatalogId != null ? resolvedCatalogId : i.getCatalogId();
        e.name = i.getName();
        e.unit = i.getUnit().name();
        e.costPrice = i.getCostPrice();
        e.active = i.isActive();
        return e;
    }
}

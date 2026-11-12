package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.catalog.entity.Ingredient;
import com.fidecode.beebite.domain.catalog.valueobject.Unit;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.IngredientEntity;

public final class IngredientMapper {
    private IngredientMapper() {
    }

    public static Ingredient toDomain(IngredientEntity e) {
        if (e == null)
            return null;
        return Ingredient.reconstitute(e.id, e.storeId, e.name, e.stock,
                e.unit != null ? Unit.valueOf(e.unit) : Unit.UN, e.costPrice, e.minStock, e.active);
    }

    public static IngredientEntity toEntity(Ingredient i) {
        IngredientEntity e = new IngredientEntity();
        e.id = i.getId();
        e.storeId = i.getStoreId();
        e.name = i.getName();
        e.stock = i.getStock();
        e.unit = i.getUnit().name();
        e.costPrice = i.getCostPrice();
        e.minStock = i.getMinStock();
        e.active = i.isActive();
        return e;
    }
}

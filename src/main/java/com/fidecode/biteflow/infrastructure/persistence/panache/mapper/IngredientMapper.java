package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.IngredientEntity;

public final class IngredientMapper {
    private IngredientMapper() {}

    public static Ingredient toDomain(IngredientEntity e) {
        if (e == null) return null;
        Ingredient i = new Ingredient(e.storeId, e.name, e.stock,
            e.unit != null ? Unit.valueOf(e.unit) : Unit.UN, e.costPrice, e.minStock);
        i.setId(e.id);
        return i;
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
        return e;
    }
}

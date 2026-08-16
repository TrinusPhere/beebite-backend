package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.Additional;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.AdditionalEntity;

public final class AdditionalMapper {
    private AdditionalMapper() {}

    public static Additional toDomain(AdditionalEntity e) {
        if (e == null) return null;
        Additional a = new Additional(e.storeId, e.name, e.price, e.category);
        a.setId(e.id);
        a.setActive(e.active);
        return a;
    }

    public static AdditionalEntity toEntity(Additional a) {
        AdditionalEntity e = new AdditionalEntity();
        e.id = a.getId();
        e.storeId = a.getStoreId();
        e.name = a.getName();
        e.price = a.getPrice();
        e.category = a.getCategory();
        e.active = a.isActive();
        return e;
    }
}

package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.catalog.entity.Additional;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.AdditionalEntity;

import java.util.UUID;

public final class AdditionalMapper {
    private AdditionalMapper() {
    }

    public static Additional toDomain(AdditionalEntity e) {
        return toDomain(e, null);
    }

    public static Additional toDomain(AdditionalEntity e, UUID contextStoreId) {
        if (e == null)
            return null;
        return Additional.reconstituteForCatalog(
                e.id,
                e.catalogId,
                contextStoreId != null ? contextStoreId : e.catalogId,
                e.name,
                e.price,
                e.category,
                e.active
        );
    }

    public static AdditionalEntity toEntity(Additional a) {
        return toEntity(a, a != null ? a.getCatalogId() : null);
    }

    public static AdditionalEntity toEntity(Additional a, UUID resolvedCatalogId) {
        AdditionalEntity e = new AdditionalEntity();
        e.id = a.getId();
        e.catalogId = resolvedCatalogId != null ? resolvedCatalogId : a.getCatalogId();
        e.name = a.getName();
        e.price = a.getPrice();
        e.category = a.getCategory();
        e.active = a.isActive();
        return e;
    }
}

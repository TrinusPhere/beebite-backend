package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CatalogEntity;

public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static Catalog toDomain(CatalogEntity e) {
        if (e == null) {
            return null;
        }
        return Catalog.reconstitute(
                e.id,
                e.tenantId,
                e.name,
                e.defaultCatalog,
                e.createdAt,
                e.updatedAt
        );
    }

    public static CatalogEntity toEntity(Catalog c) {
        if (c == null) {
            return null;
        }
        CatalogEntity e = new CatalogEntity();
        e.id = c.getId();
        e.tenantId = c.getTenantId();
        e.name = c.getName();
        e.defaultCatalog = c.isDefaultCatalog();
        return e;
    }
}


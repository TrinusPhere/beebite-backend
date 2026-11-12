package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.production.entity.ProductionSector;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.ProductionSectorEntity;

public final class ProductionSectorMapper {
    private ProductionSectorMapper() {
    }

    public static ProductionSector toDomain(ProductionSectorEntity e) {
        return ProductionSector.reconstitute(e.id, e.storeId, e.code, e.name, e.active);
    }

    public static ProductionSectorEntity toEntity(ProductionSector s) {
        var e = new ProductionSectorEntity();
        e.id = s.getId();
        e.storeId = s.getStoreId();
        e.code = s.getCode();
        e.name = s.getName();
        e.active = s.isActive();
        return e;
    }
}

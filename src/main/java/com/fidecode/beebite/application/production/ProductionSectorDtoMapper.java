package com.fidecode.beebite.application.production;

import com.fidecode.beebite.application.production.dto.ProductionSectorResponse;
import com.fidecode.beebite.domain.production.entity.ProductionSector;

public final class ProductionSectorDtoMapper {
    private ProductionSectorDtoMapper() {
    }

    public static ProductionSectorResponse toResponse(ProductionSector s) {
        return new ProductionSectorResponse(s.getId() != null ? s.getId().toString() : null, s.getCode(), s.getName(),
                s.isActive());
    }
}

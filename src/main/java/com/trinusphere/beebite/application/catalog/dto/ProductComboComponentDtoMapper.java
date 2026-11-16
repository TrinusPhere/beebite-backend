package com.trinusphere.beebite.application.catalog.dto;

import com.trinusphere.beebite.domain.catalog.entity.ProductComboComponent;

public final class ProductComboComponentDtoMapper {

    private ProductComboComponentDtoMapper() {}

    public static ProductComboComponentResponse toResponse(ProductComboComponent c, String componentProductName) {
        if (c == null) return null;
        return new ProductComboComponentResponse(
                c.getId() != null ? c.getId().toString() : null,
                c.getComboProductId() != null ? c.getComboProductId().toString() : null,
                c.getComponentProductId() != null ? c.getComponentProductId().toString() : null,
                componentProductName,
                c.getQuantity()
        );
    }
}


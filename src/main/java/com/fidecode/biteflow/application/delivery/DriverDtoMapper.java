package com.fidecode.biteflow.application.delivery;

import com.fidecode.biteflow.application.delivery.dto.DriverResponse;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;

/**
 * Mapper centralizado para conversão DeliveryDriver → DriverResponse.
 */
public final class DriverDtoMapper {

    private DriverDtoMapper() {}

    public static DriverResponse toResponse(DeliveryDriver d) {
        return new DriverResponse(
            d.getId() != null ? d.getId().toString() : null,
            d.getName(),
            d.getPhone(),
            d.getStatus().name(),
            d.isFixed(),
            d.isDaily(),
            d.getAllowance(),
            d.isHasFixedDeliveryValue(),
            d.getFixedDeliveryValue(),
            d.getTotalDeliveries(),
            d.getRating(),
            d.getCreatedAt() != null ? d.getCreatedAt().toString() : null
        );
    }
}

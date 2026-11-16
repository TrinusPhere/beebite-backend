package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.delivery.entity.DeliveryDriver;
import com.trinusphere.beebite.domain.delivery.valueobject.DriverStatus;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.DeliveryDriverEntity;

public class DeliveryDriverMapper {

    public static DeliveryDriver toDomain(DeliveryDriverEntity e) {
        return DeliveryDriver.reconstitute(e.getId(), e.getStoreId(), e.getName(), e.getPhone(),
                DriverStatus.valueOf(e.getStatus()), e.isFixed(), e.isDaily(), e.getAllowance(),
                e.isHasFixedDeliveryValue(), e.getFixedDeliveryValue(), e.getTotalDeliveries(), e.getRating(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    public static DeliveryDriverEntity toEntity(DeliveryDriver d) {
        DeliveryDriverEntity e = new DeliveryDriverEntity();
        e.setId(d.getId());
        e.setStoreId(d.getStoreId());
        e.setName(d.getName());
        e.setPhone(d.getPhone());
        e.setStatus(d.getStatus().name());
        e.setFixed(d.isFixed());
        e.setDaily(d.isDaily());
        e.setAllowance(d.getAllowance());
        e.setHasFixedDeliveryValue(d.isHasFixedDeliveryValue());
        e.setFixedDeliveryValue(d.getFixedDeliveryValue());
        e.setTotalDeliveries(d.getTotalDeliveries());
        e.setRating(d.getRating());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}

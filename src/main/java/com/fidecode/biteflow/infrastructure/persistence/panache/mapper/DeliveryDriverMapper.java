package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.DeliveryDriverEntity;

public class DeliveryDriverMapper {

    public static DeliveryDriver toDomain(DeliveryDriverEntity e) {
        DeliveryDriver d = new DeliveryDriver();
        d.setId(e.getId());
        d.setStoreId(e.getStoreId());
        d.setName(e.getName());
        d.setPhone(e.getPhone());
        d.setStatus(DriverStatus.valueOf(e.getStatus()));
        d.setFixed(e.isFixed());
        d.setDaily(e.isDaily());
        d.setAllowance(e.getAllowance());
        d.setHasFixedDeliveryValue(e.isHasFixedDeliveryValue());
        d.setFixedDeliveryValue(e.getFixedDeliveryValue());
        d.setTotalDeliveries(e.getTotalDeliveries());
        d.setRating(e.getRating());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
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

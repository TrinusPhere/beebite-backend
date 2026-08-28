package com.fidecode.biteflow.domain.delivery.repository;

import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryDriverRepository {
    DeliveryDriver save(DeliveryDriver driver);

    Optional<DeliveryDriver> findById(UUID id);

    com.fidecode.biteflow.domain.shared.PageResult<DeliveryDriver> findAllByStoreId(UUID storeId, int page, int size);

    void deleteById(UUID id);
}

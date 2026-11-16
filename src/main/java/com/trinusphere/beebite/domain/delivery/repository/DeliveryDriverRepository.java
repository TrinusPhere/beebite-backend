package com.trinusphere.beebite.domain.delivery.repository;

import com.trinusphere.beebite.domain.delivery.entity.DeliveryDriver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryDriverRepository {
    DeliveryDriver save(DeliveryDriver driver);

    Optional<DeliveryDriver> findById(UUID id);

    com.trinusphere.beebite.domain.shared.PageResult<DeliveryDriver> findAllByStoreId(UUID storeId, int page, int size);

    void deleteById(UUID id);
}

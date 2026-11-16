package com.trinusphere.beebite.domain.catalog.repository;

import com.trinusphere.beebite.domain.catalog.entity.Additional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdditionalRepository {
    Optional<Additional> findById(UUID id);

    com.trinusphere.beebite.domain.shared.PageResult<Additional> findAllByStoreId(UUID storeId, int page, int size);

    Additional save(Additional additional);

    void deleteById(UUID id);
}

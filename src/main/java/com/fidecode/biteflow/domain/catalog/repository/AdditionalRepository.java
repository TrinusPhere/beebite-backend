package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.Additional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdditionalRepository {
    Optional<Additional> findById(UUID id);

    com.fidecode.biteflow.domain.shared.PageResult<Additional> findAllByStoreId(UUID storeId, int page, int size);

    Additional save(Additional additional);

    void deleteById(UUID id);
}

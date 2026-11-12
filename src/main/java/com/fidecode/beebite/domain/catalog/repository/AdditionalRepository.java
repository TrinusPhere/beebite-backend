package com.fidecode.beebite.domain.catalog.repository;

import com.fidecode.beebite.domain.catalog.entity.Additional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdditionalRepository {
    Optional<Additional> findById(UUID id);

    com.fidecode.beebite.domain.shared.PageResult<Additional> findAllByStoreId(UUID storeId, int page, int size);

    Additional save(Additional additional);

    void deleteById(UUID id);
}

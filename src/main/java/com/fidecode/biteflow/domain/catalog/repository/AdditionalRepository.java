package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.Additional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdditionalRepository {
    Optional<Additional> findById(UUID id);
    List<Additional> findAllByStoreId(UUID storeId);
    Additional save(Additional additional);
    void deleteById(UUID id);
}

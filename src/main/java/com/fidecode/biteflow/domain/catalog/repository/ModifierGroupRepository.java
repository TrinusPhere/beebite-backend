package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModifierGroupRepository {
    Optional<ModifierGroup> findById(UUID id);

    com.fidecode.biteflow.domain.shared.PageResult<ModifierGroup> findAllByStoreId(UUID storeId, int page, int size);

    List<ModifierGroup> findAllById(List<UUID> ids);

    ModifierGroup save(ModifierGroup modifierGroup);

    void deleteById(UUID id);
}

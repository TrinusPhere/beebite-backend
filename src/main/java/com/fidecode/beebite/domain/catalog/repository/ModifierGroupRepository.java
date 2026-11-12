package com.fidecode.beebite.domain.catalog.repository;

import com.fidecode.beebite.domain.catalog.entity.ModifierGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModifierGroupRepository {
    Optional<ModifierGroup> findById(UUID id);

    com.fidecode.beebite.domain.shared.PageResult<ModifierGroup> findAllByStoreId(UUID storeId, int page, int size);

    List<ModifierGroup> findAllByStoreId(UUID storeId);

    List<ModifierGroup> findAllById(List<UUID> ids);

    ModifierGroup save(ModifierGroup modifierGroup);

    void deleteById(UUID id);
}

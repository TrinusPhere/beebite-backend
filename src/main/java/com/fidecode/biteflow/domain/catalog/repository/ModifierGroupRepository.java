package com.fidecode.biteflow.domain.catalog.repository;

import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModifierGroupRepository {
    Optional<ModifierGroup> findById(UUID id);
    List<ModifierGroup> findAllByStoreId(UUID storeId);
    ModifierGroup save(ModifierGroup modifierGroup);
    void deleteById(UUID id);
}

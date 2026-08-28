package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.exception.ModifierGroupNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteModifierGroupUseCase {

    private final ModifierGroupRepository repository;

    @Inject
    public DeleteModifierGroupUseCase(ModifierGroupRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID storeId, UUID id) {
        ModifierGroup group = repository.findById(id)
                .orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));
        StoreGuard.enforce(group, storeId);
        repository.deleteById(id);
    }
}

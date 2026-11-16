package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.exception.ModifierGroupNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
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

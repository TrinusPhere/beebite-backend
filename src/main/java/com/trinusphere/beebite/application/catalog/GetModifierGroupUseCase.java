package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.ModifierGroupResponse;
import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.exception.ModifierGroupNotFoundException;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetModifierGroupUseCase {

    private final ModifierGroupRepository repository;

    @Inject
    public GetModifierGroupUseCase(ModifierGroupRepository repository) {
        this.repository = repository;
    }

    public ModifierGroupResponse execute(UUID storeId, UUID id) {
        var inStore = repository.findByIdInStore(id, storeId);
        ModifierGroup group = (inStore.isPresent() ? inStore : repository.findById(id))
                .orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));
        StoreGuard.enforce(group, storeId);
        return ModifierGroupDtoMapper.toResponse(group);
    }
}

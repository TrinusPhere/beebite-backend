package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.beebite.domain.catalog.entity.ModifierGroup;
import com.fidecode.beebite.domain.catalog.exception.ModifierGroupNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.beebite.domain.shared.StoreGuard;
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
        ModifierGroup group = repository.findById(id)
                .orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));
        StoreGuard.enforce(group, storeId);
        return ModifierGroupDtoMapper.toResponse(group);
    }
}

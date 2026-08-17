package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.biteflow.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.entity.ModifierOption;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.ModifierType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CreateModifierGroupUseCase {

    private final ModifierGroupRepository repository;

    @Inject
    public CreateModifierGroupUseCase(ModifierGroupRepository repository) {
        this.repository = repository;
    }

    public ModifierGroupResponse execute(UUID storeId, ModifierGroupRequest request) {
        ModifierType type = request.type() != null ? ModifierType.valueOf(request.type()) : ModifierType.RADIO;
        boolean required = request.required() != null ? request.required() : false;
        int min = request.minSelections() != null ? request.minSelections() : 0;
        int max = request.maxSelections() != null ? request.maxSelections() : 1;

        ModifierGroup group = new ModifierGroup(storeId, request.name(), type, required, min, max, request.icon());

        if (request.options() != null) {
            List<ModifierOption> options = new ArrayList<>();
            for (int i = 0; i < request.options().size(); i++) {
                var optDto = request.options().get(i);
                options.add(new ModifierOption(null, optDto.name(),
                    optDto.price() != null ? optDto.price() : BigDecimal.ZERO, i));
            }
            group.setOptions(options);
        }

        return ModifierGroupDtoMapper.toResponse(repository.save(group));
    }
}

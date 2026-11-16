package com.trinusphere.beebite.application.catalog;

import com.trinusphere.beebite.application.catalog.dto.ModifierGroupRequest;
import com.trinusphere.beebite.application.catalog.dto.ModifierGroupResponse;
import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.entity.ModifierOption;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.domain.catalog.valueobject.ModifierType;
import com.trinusphere.beebite.domain.catalog.valueobject.ModifierGroupSemanticType;
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
        ModifierGroupSemanticType semanticType = request.semanticType() != null ? ModifierGroupSemanticType.valueOf(request.semanticType()) : null;

        ModifierGroup group = new ModifierGroup(storeId, request.name(), type, required, min, max, request.icon(), semanticType);

        if (request.options() != null) {
            List<ModifierOption> options = new ArrayList<>();
            for (int i = 0; i < request.options().size(); i++) {
                var optDto = request.options().get(i);
                ModifierOption opt = new ModifierOption(null, optDto.name(),
                        optDto.price() != null ? optDto.price() : BigDecimal.ZERO, i);
                if (optDto.linkedProductId() != null) {
                    opt.setLinkedProductId(UUID.fromString(optDto.linkedProductId()));
                }
                options.add(opt);
            }
            group.replaceOptions(options);
        }

        return ModifierGroupDtoMapper.toResponse(repository.save(group));
    }
}

package com.fidecode.beebite.application.catalog;

import com.fidecode.beebite.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.beebite.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.beebite.domain.catalog.entity.ModifierGroup;
import com.fidecode.beebite.domain.catalog.entity.ModifierOption;
import com.fidecode.beebite.domain.catalog.exception.ModifierGroupNotFoundException;
import com.fidecode.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.beebite.domain.catalog.valueobject.ModifierType;
import com.fidecode.beebite.domain.catalog.valueobject.ModifierGroupSemanticType;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UpdateModifierGroupUseCase {

    private final ModifierGroupRepository repository;

    @Inject
    public UpdateModifierGroupUseCase(ModifierGroupRepository repository) {
        this.repository = repository;
    }

    public ModifierGroupResponse execute(UUID storeId, UUID id, ModifierGroupRequest request) {
        ModifierGroup group = repository.findById(id)
                .orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));
        StoreGuard.enforce(group, storeId);

        ModifierGroupSemanticType semanticType = request.semanticType() != null ? ModifierGroupSemanticType.valueOf(request.semanticType()) : null;

        group.update(request.name(), request.type() != null ? ModifierType.valueOf(request.type()) : null,
                request.required(), request.minSelections(), request.maxSelections(), request.icon(), semanticType);

        if (request.options() != null) {
            List<ModifierOption> options = new ArrayList<>();
            for (int i = 0; i < request.options().size(); i++) {
                var optDto = request.options().get(i);
                ModifierOption opt = new ModifierOption(group.getId(), optDto.name(),
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

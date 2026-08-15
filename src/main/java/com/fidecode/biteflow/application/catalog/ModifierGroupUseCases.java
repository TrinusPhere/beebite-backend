package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.ModifierGroupRequest;
import com.fidecode.biteflow.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.entity.ModifierOption;
import com.fidecode.biteflow.domain.catalog.exception.ModifierGroupNotFoundException;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.domain.catalog.valueobject.ModifierType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ModifierGroupUseCases {

    private final ModifierGroupRepository repository;

    @Inject
    public ModifierGroupUseCases(ModifierGroupRepository repository) {
        this.repository = repository;
    }

    public ModifierGroupResponse create(UUID storeId, ModifierGroupRequest request) {
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

        return toResponse(repository.save(group));
    }

    public ModifierGroupResponse update(UUID id, ModifierGroupRequest request) {
        ModifierGroup group = repository.findById(id)
            .orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));

        group.setName(request.name());
        if (request.type() != null) group.setType(ModifierType.valueOf(request.type()));
        if (request.required() != null) group.setRequired(request.required());
        if (request.minSelections() != null) group.setMinSelections(request.minSelections());
        if (request.maxSelections() != null) group.setMaxSelections(request.maxSelections());
        group.setIcon(request.icon());

        if (request.options() != null) {
            List<ModifierOption> options = new ArrayList<>();
            for (int i = 0; i < request.options().size(); i++) {
                var optDto = request.options().get(i);
                options.add(new ModifierOption(group.getId(), optDto.name(),
                    optDto.price() != null ? optDto.price() : BigDecimal.ZERO, i));
            }
            group.setOptions(options);
        }

        return toResponse(repository.save(group));
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));
        repository.deleteById(id);
    }

    public ModifierGroupResponse getById(UUID id) {
        return repository.findById(id)
            .map(ModifierGroupUseCases::toResponse)
            .orElseThrow(() -> new ModifierGroupNotFoundException(id.toString()));
    }

    public List<ModifierGroupResponse> list(UUID storeId) {
        return repository.findAllByStoreId(storeId).stream().map(ModifierGroupUseCases::toResponse).toList();
    }

    static ModifierGroupResponse toResponse(ModifierGroup g) {
        List<ModifierGroupResponse.ModifierOptionResponse> opts = g.getOptions() != null
            ? g.getOptions().stream().map(o -> new ModifierGroupResponse.ModifierOptionResponse(
                o.getId() != null ? o.getId().toString() : null, o.getName(), o.getPrice()
            )).toList()
            : List.of();

        return new ModifierGroupResponse(
            g.getId() != null ? g.getId().toString() : null,
            g.getName(), g.getType().name(), g.isRequired(),
            g.getMinSelections(), g.getMaxSelections(), g.getIcon(), opts
        );
    }
}

package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.ModifierGroupResponse;
import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;

import java.util.List;

/**
 * Mapper centralizado para conversão ModifierGroup → ModifierGroupResponse.
 */
public final class ModifierGroupDtoMapper {

    private ModifierGroupDtoMapper() {
    }

    public static ModifierGroupResponse toResponse(ModifierGroup g) {
        List<ModifierGroupResponse.ModifierOptionResponse> opts = g.getOptions() != null
                ? g.getOptions().stream()
                        .map(o -> new ModifierGroupResponse.ModifierOptionResponse(
                                o.getId() != null ? o.getId().toString() : null, o.getName(), o.getPrice(),
                                o.getLinkedProductId() != null ? o.getLinkedProductId().toString() : null))
                        .toList()
                : List.of();

        return new ModifierGroupResponse(g.getId() != null ? g.getId().toString() : null, g.getName(),
                g.getType().name(), g.isRequired(), g.getMinSelections(), g.getMaxSelections(), g.getIcon(),
                g.getSemanticType() != null ? g.getSemanticType().name() : null, opts);
    }
}

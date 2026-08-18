package com.fidecode.biteflow.application.catalog;

import com.fidecode.biteflow.application.catalog.dto.AdditionalResponse;
import com.fidecode.biteflow.domain.catalog.entity.Additional;

/**
 * Mapper centralizado para conversão Additional → AdditionalResponse.
 * Reutilizado por todos os Use Cases de Additional.
 */
public final class AdditionalDtoMapper {

    private AdditionalDtoMapper() {}

    public static AdditionalResponse toResponse(Additional a) {
        return new AdditionalResponse(
            a.getId() != null ? a.getId().toString() : null,
            a.getName(), a.getPrice(), a.getCategory(), a.isActive()
        );
    }
}

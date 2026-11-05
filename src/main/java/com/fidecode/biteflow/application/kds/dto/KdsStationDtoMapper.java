package com.fidecode.biteflow.application.kds.dto;

import com.fidecode.biteflow.domain.kds.entity.KdsStation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class KdsStationDtoMapper {

    private KdsStationDtoMapper() {}

    public static KdsStationResponse toResponse(KdsStation s) {
        if (s == null) return null;
        return new KdsStationResponse(
                s.getId() != null ? s.getId().toString() : null,
                s.getName(),
                s.isExpedition(),
                s.isActive(),
                s.getSectorIds() != null ? s.getSectorIds().stream().map(UUID::toString).collect(Collectors.toList()) : List.of(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}

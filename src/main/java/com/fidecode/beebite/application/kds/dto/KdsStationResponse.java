package com.fidecode.beebite.application.kds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record KdsStationResponse(
        String id,
        String name,
        boolean isExpedition,
        boolean active,
        List<String> sectorIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}


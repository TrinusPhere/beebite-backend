package com.trinusphere.beebite.application.kds.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record KdsStationRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        boolean isExpedition,
        List<String> sectorIds
) {}


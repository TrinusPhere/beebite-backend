package com.fidecode.biteflow.application.production.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductionSectorRequest(@NotBlank(message = "Nome é obrigatório") String name, String code,
        Boolean active) {
}

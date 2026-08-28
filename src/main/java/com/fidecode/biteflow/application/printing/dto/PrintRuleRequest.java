package com.fidecode.biteflow.application.printing.dto;

import jakarta.validation.constraints.NotBlank;

public record PrintRuleRequest(@NotBlank String name, @NotBlank String operation, @NotBlank String documentType,
        String sectorId, @NotBlank String printerId, Boolean active) {
}

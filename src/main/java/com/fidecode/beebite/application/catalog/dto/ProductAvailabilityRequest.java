package com.fidecode.beebite.application.catalog.dto;

import jakarta.validation.constraints.NotNull;

public record ProductAvailabilityRequest(
        @NotNull(message = "status é obrigatório") String status,
        @NotNull(message = "availableDigitalMenu é obrigatório") Boolean availableDigitalMenu,
        @NotNull(message = "availablePdv é obrigatório") Boolean availablePdv) {
}

package com.trinusphere.beebite.application.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductComboComponentRequest(
        @NotBlank(message = "componentProductId é obrigatório") String componentProductId,
        @Min(value = 1, message = "quantity deve ser maior que zero") int quantity
) {}


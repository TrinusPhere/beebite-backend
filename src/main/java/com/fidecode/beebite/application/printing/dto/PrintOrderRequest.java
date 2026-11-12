package com.fidecode.beebite.application.printing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrintOrderRequest(@NotNull java.util.UUID printerId, @NotBlank String documentType) {
}


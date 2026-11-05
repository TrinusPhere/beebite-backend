package com.fidecode.biteflow.application.cash.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OpenCashSessionRequest(
        @NotBlank(message = "cashRegisterId é obrigatório") String cashRegisterId,
        @NotNull(message = "initialAmount é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "initialAmount não pode ser negativo") BigDecimal initialAmount
) {}

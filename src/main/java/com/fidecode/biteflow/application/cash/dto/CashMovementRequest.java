package com.fidecode.biteflow.application.cash.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashMovementRequest(
        @NotBlank(message = "type é obrigatório (SUPRIMENTO, SANGRIA, AJUSTE_COMPENSATORIO)") String type,
        @NotNull(message = "amount é obrigatório")
        @DecimalMin(value = "0.01", message = "amount deve ser maior que zero") BigDecimal amount,
        @NotBlank(message = "reason é obrigatório") String reason
) {}

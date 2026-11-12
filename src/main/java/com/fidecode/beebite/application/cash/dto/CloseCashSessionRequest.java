package com.fidecode.beebite.application.cash.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CloseCashSessionRequest(
        @NotNull(message = "countedAmount é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "countedAmount não pode ser negativo") BigDecimal countedAmount,
        String closingNotes
) {}

package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSaasInvoiceRequest(
        @NotNull(message = "tenantId é obrigatório.") UUID tenantId,
        @NotNull(message = "contractId é obrigatório.") UUID contractId,
        @NotBlank(message = "A competência é obrigatória.") String referenceMonth,
        @NotBlank(message = "A descrição é obrigatória.") String description,
        @NotNull(message = "O valor é obrigatório.") BigDecimal amount,
        @NotNull(message = "O vencimento é obrigatório.") LocalDate dueDate
) {
}


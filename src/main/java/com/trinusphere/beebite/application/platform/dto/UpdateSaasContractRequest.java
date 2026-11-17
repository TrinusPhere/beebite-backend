package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateSaasContractRequest(
        @NotBlank(message = "O plano é obrigatório.") String planName,
        @NotBlank(message = "O ciclo de cobrança é obrigatório.") String billingCycle,
        @NotNull(message = "A mensalidade é obrigatória.") BigDecimal monthlyFee,
        int maxStores,
        boolean includesIfood,
        boolean includesEdgeSync,
        int billingDay,
        String status,
        String contractNotes
) {
}


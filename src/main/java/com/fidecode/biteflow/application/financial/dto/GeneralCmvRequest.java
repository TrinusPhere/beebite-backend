package com.fidecode.biteflow.application.financial.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request — cálculo de CMV geral.
 * Recebe as datas como epoch (milissegundos) e os valores de estoque.
 */
public record GeneralCmvRequest(
    @NotNull(message = "startDate é obrigatório") Long startDate,
    @NotNull(message = "endDate é obrigatório") Long endDate,
    @NotNull(message = "initialStock é obrigatório") BigDecimal initialStock,
    @NotNull(message = "purchasesInPeriod é obrigatório") BigDecimal purchasesInPeriod,
    @NotNull(message = "finalStock é obrigatório") BigDecimal finalStock
) {}

package com.fidecode.biteflow.application.financial.dto;

import java.math.BigDecimal;

/**
 * Response — resultado do cálculo de CMV geral.
 */
public record GeneralCmvResponse(BigDecimal grossRevenue, BigDecimal cmvValue, BigDecimal cmvPercentage,
        String alertLevel) {
}

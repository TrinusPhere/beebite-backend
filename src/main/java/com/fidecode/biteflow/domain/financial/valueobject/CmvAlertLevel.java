package com.fidecode.biteflow.domain.financial.valueobject;

import java.math.BigDecimal;

/**
 * Nível de alerta do CMV. POJO puro — sem dependência de framework.
 *
 * <p>
 * Regra de negócio:
 * <ul>
 * <li>GREEN → CMV < 30%</li>
 * <li>YELLOW → 30% ≤ CMV ≤ 35%</li>
 * <li>RED → CMV > 35%</li>
 * </ul>
 */
public enum CmvAlertLevel {
    GREEN, YELLOW, RED;

    private static final BigDecimal THRESHOLD_GREEN = new BigDecimal("30");
    private static final BigDecimal THRESHOLD_RED = new BigDecimal("35");

    /**
     * Determina o nível de alerta com base na porcentagem de CMV.
     *
     * @param cmvPercentage
     *            porcentagem de CMV (ex: 24.5 para 24.5%)
     *
     * @return nível de alerta correspondente
     */
    public static CmvAlertLevel fromPercentage(BigDecimal cmvPercentage) {
        if (cmvPercentage == null) {
            return GREEN;
        }
        if (cmvPercentage.compareTo(THRESHOLD_GREEN) < 0) {
            return GREEN;
        }
        if (cmvPercentage.compareTo(THRESHOLD_RED) > 0) {
            return RED;
        }
        return YELLOW;
    }
}

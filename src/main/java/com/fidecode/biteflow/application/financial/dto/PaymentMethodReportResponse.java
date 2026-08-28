package com.fidecode.biteflow.application.financial.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response — distribuição por forma de pagamento.
 */
public record PaymentMethodReportResponse(long startDate, long endDate, List<MethodEntry> methods) {
    public record MethodEntry(String method, int quantity, BigDecimal totalValue) {
    }
}

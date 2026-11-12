package com.fidecode.beebite.application.financial.dto;

import java.math.BigDecimal;

/**
 * Response — resumo de vendas por período.
 */
public record SalesSummaryResponse(long startDate, long endDate, BigDecimal revenue, String changePercentage,
        int totalOrders) {
}

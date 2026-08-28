package com.fidecode.biteflow.application.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsOverviewResponse(BigDecimal grossRevenue, BigDecimal netRevenue, BigDecimal cmv,
        BigDecimal grossMargin, BigDecimal averageTicket, long orders, BigDecimal fees, BigDecimal discounts,
        List<Breakdown> channels, List<Breakdown> operations, List<String> alerts) {
    public record Breakdown(String name, BigDecimal value) {
    }
}

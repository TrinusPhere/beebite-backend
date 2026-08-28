package com.fidecode.biteflow.application.delivery.dto;

import java.math.BigDecimal;
import java.util.List;

public record SettlementReportResponse(String period, List<SettlementEntry> entries, BigDecimal grandTotal) {
    public record SettlementEntry(String driverId, String driverName, String driverType, int deliveries,
            BigDecimal allowance, BigDecimal feePerDelivery, BigDecimal total) {
    }
}

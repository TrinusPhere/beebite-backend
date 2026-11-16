package com.trinusphere.beebite.application.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SettlementReportResponse(String period, List<SettlementEntry> entries, BigDecimal grandTotal) {
    public record SettlementEntry(
            String driverId,
            String driverName,
            String driverType,
            int deliveries,
            BigDecimal allowance,
            BigDecimal feePerDelivery,
            BigDecimal total,
            List<SettlementOrderDto> orders) {

        public SettlementEntry(
                String driverId,
                String driverName,
                String driverType,
                int deliveries,
                BigDecimal allowance,
                BigDecimal feePerDelivery,
                BigDecimal total) {
            this(driverId, driverName, driverType, deliveries, allowance, feePerDelivery, total, List.of());
        }
    }

    public record SettlementOrderDto(
            String id,
            Long displayId,
            String customerName,
            String customerAddress,
            BigDecimal totalValue,
            BigDecimal deliveryCost,
            String status,
            String paymentMethod,
            LocalDateTime createdAt) {
    }
}

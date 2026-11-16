package com.trinusphere.beebite.application.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Response — registro financeiro individual.
 */
public record FinancialRecordResponse(UUID id, UUID orderId, LocalDate recordDate, LocalTime recordTime,
        BigDecimal totalValue, String paymentMethod, String status, List<FinancialRecordProductDto> products) {
    public record FinancialRecordProductDto(UUID productId, String productName, int quantity, BigDecimal price) {
    }
}

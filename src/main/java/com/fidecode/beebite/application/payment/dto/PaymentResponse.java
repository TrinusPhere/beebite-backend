package com.fidecode.beebite.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String id,
        String orderId,
        String cashSessionId,
        String driverId,
        String method,
        BigDecimal amount,
        BigDecimal changeFor,
        String status,
        String collectedBy,
        String notes,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {}

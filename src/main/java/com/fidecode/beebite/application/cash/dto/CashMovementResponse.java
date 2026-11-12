package com.fidecode.beebite.application.cash.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashMovementResponse(
        String id,
        String cashSessionId,
        String userId,
        String type,
        BigDecimal amount,
        String reason,
        LocalDateTime createdAt
) {}

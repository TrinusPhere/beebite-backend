package com.fidecode.beebite.application.cash.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashSessionResponse(
        String id,
        String cashRegisterId,
        String userId,
        String status,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        BigDecimal initialAmount,
        BigDecimal expectedAmount,
        BigDecimal countedAmount,
        BigDecimal differenceAmount,
        String closingNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

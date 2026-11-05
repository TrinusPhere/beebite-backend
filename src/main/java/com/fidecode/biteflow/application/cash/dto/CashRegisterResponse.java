package com.fidecode.biteflow.application.cash.dto;

import java.time.LocalDateTime;

public record CashRegisterResponse(
        String id,
        String name,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

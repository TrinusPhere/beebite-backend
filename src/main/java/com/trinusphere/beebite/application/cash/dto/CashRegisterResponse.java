package com.trinusphere.beebite.application.cash.dto;

import java.time.LocalDateTime;

public record CashRegisterResponse(
        String id,
        String name,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

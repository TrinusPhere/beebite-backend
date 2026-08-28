package com.fidecode.biteflow.application.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record IngredientResponse(
    String id,
    String name,
    BigDecimal stock,
    String unit,
    BigDecimal costPrice,
    BigDecimal minStock,
    boolean active,
    boolean lowStock,
    Instant createdAt
) {}

package com.fidecode.biteflow.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record IngredientRequest(
    @NotBlank(message = "Nome é obrigatório") String name,
    BigDecimal stock,
    String unit,
    BigDecimal costPrice,
    BigDecimal minStock
) {}

package com.fidecode.beebite.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record IngredientRequest(@NotBlank(message = "Nome é obrigatório") String name, BigDecimal stock, String unit,
        BigDecimal costPrice, BigDecimal minStock, Boolean active) {
    /** Compatibilidade com o contrato anterior. */
    public IngredientRequest(String name, BigDecimal stock, String unit, BigDecimal costPrice, BigDecimal minStock) {
        this(name, stock, unit, costPrice, minStock, null);
    }
}

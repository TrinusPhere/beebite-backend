package com.fidecode.biteflow.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AdditionalRequest(@NotBlank(message = "Nome é obrigatório") String name, BigDecimal price,
        String category) {
}

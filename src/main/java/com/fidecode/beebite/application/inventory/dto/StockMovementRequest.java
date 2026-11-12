package com.fidecode.beebite.application.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StockMovementRequest(@NotBlank String ingredientId, @NotBlank String type,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity, @NotBlank String reason, String document,
        String sourceType, String sourceId) {
}

package com.trinusphere.beebite.application.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateOrderItemDto(@NotBlank(message = "productId é obrigatório") String productId,
        @Min(value = 1, message = "quantity deve ser ao menos 1") int quantity, String observation,
        List<ModifierDto> modifiers, List<String> removalIds) {
    public record ModifierDto(String groupId, String optionId) {
    }
}

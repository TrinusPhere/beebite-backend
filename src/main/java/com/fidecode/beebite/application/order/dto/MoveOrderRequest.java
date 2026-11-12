package com.fidecode.beebite.application.order.dto;

import jakarta.validation.constraints.NotBlank;

public record MoveOrderRequest(@NotBlank(message = "targetStatus é obrigatório") String targetStatus) {
}

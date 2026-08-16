package com.fidecode.biteflow.application.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "type é obrigatório") String type,
    CustomerInfo customer,
    @NotBlank(message = "paymentMethod é obrigatório") String paymentMethod,
    @NotNull(message = "items é obrigatório") @Size(min = 1, message = "Pelo menos um item é obrigatório")
    List<@Valid CreateOrderItemDto> items,
    BigDecimal deliveryCost
) {
    public record CustomerInfo(String id, String name, String address) {}
}

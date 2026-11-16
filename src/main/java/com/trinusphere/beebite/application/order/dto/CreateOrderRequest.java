package com.trinusphere.beebite.application.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(@NotBlank(message = "type é obrigatório") String type, CustomerInfo customer,
        @NotBlank(message = "paymentMethod é obrigatório") String paymentMethod,
        @NotNull(message = "items é obrigatório") @Size(min = 1, message = "Pelo menos um item é obrigatório") List<@Valid CreateOrderItemDto> items,
        BigDecimal deliveryCost, String operation, String channel, String serviceType, String source,
        BigDecimal cashChangeFor, String notes) {

    public CreateOrderRequest(String type, CustomerInfo customer, String paymentMethod, List<CreateOrderItemDto> items,
            BigDecimal deliveryCost) {
        this(type, customer, paymentMethod, items, deliveryCost, null, null, null, null, null, null);
    }
    public record CustomerInfo(String id, String name, String address, String phone, String complement,
            String reference) {
        public CustomerInfo(String id, String name, String address) {
            this(id, name, address, null, null, null);
        }
    }
}

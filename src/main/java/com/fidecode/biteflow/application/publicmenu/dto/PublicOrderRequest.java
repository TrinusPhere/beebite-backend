package com.fidecode.biteflow.application.publicmenu.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record PublicOrderRequest(String customerId, @NotBlank String customerName, @NotBlank String customerPhone,
        @NotBlank String serviceType, String operation, String channel, String source, DeliveryAddress deliveryAddress,
        @NotBlank String paymentMethod, BigDecimal cashChangeFor, String notes, BigDecimal total,
        @NotNull @Size(min = 1) List<Item> items) {
    public record DeliveryAddress(String address, String complement, String reference) {
    }

    public record Item(@NotBlank String productId, @Min(1) int quantity, BigDecimal unitPrice, String name) {
    }
}

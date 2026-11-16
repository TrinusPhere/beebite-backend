package com.trinusphere.beebite.application.order.dto;

import java.util.List;

public record UpdateOrderRequest(String type, CreateOrderRequest.CustomerInfo customer, String paymentMethod,
        List<CreateOrderItemDto> newItems, String operation, String channel, String serviceType, String source) {
    public UpdateOrderRequest(String type, CreateOrderRequest.CustomerInfo customer, String paymentMethod,
            List<CreateOrderItemDto> newItems) {
        this(type, customer, paymentMethod, newItems, null, null, null, null);
    }
}

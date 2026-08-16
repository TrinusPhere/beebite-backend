package com.fidecode.biteflow.application.order.dto;

import java.util.List;

public record UpdateOrderRequest(
    String type,
    CreateOrderRequest.CustomerInfo customer,
    String paymentMethod,
    List<CreateOrderItemDto> additionalItems
) {}

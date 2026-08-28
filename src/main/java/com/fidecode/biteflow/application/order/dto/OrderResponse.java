package com.fidecode.biteflow.application.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
    String id,
    String displayId,
    String customerId,
    String customerName,
    String customerAddress,
    String customerPhone,
    String customerAddressComplement,
    String customerAddressReference,
    String type,
    String operation,
    String channel,
    String serviceType,
    String source,
    String status,
    String paymentMethod,
    BigDecimal cashChangeFor,
    String notes,
    String driverId,
    String driverName,
    String platform,
    String externalOrderId,
    BigDecimal subtotal,
    BigDecimal deliveryCost,
    BigDecimal totalValue,
    boolean nfeEmitted,
    List<OrderItemResponse> items,
    String createdAt,
    String finishedAt
) {
    public record OrderItemResponse(String id, String productId, String productName, int quantity, BigDecimal unitPrice,
        BigDecimal extraPrice, BigDecimal totalPrice, String observation, List<ModifierResponse> modifiers, List<RemovalResponse> removals) {}
    public record ModifierResponse(String id, String groupId, String groupName, String optionId, String optionName, BigDecimal price) {}
    public record RemovalResponse(String id, String ingredientId, String ingredientName) {}
}

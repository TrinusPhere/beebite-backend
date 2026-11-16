package com.trinusphere.beebite.application.order.dto;

public record PublicOrderReviewInfoResponse(String orderId, String displayId, String driverName, String customerName,
        String customerPhone) {
}

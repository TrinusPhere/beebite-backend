package com.trinusphere.beebite.application.delivery.dto;

import java.math.BigDecimal;

public record DriverResponse(String id, String name, String phone, String status, boolean isFixed, boolean isDaily,
        BigDecimal allowance, boolean hasFixedDeliveryValue, BigDecimal fixedDeliveryValue, int totalDeliveries,
        BigDecimal rating, String createdAt) {
}

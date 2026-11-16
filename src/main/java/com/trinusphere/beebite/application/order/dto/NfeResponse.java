package com.trinusphere.beebite.application.order.dto;

public record NfeResponse(String orderId, String nfeNumber, String nfeKey, String emittedAt, String status) {
}

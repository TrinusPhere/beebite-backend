package com.fidecode.beebite.application.customer.dto;

public record CustomerResponse(String id, String name, String phone, String email, String zipCode, String address,
        String number, String complement, String neighborhood, int totalOrders, String createdAt) {
}

package com.fidecode.beebite.application.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(@NotBlank(message = "name é obrigatório") String name, String phone, String email,
        String zipCode, String address, String number, String complement, String neighborhood) {
}

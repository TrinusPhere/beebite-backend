package com.fidecode.biteflow.application.cash.dto;

import jakarta.validation.constraints.NotBlank;

public record CashRegisterRequest(
        @NotBlank(message = "Nome é obrigatório") String name
) {}

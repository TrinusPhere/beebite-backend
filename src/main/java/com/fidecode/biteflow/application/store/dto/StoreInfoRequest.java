package com.fidecode.biteflow.application.store.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreInfoRequest(
    @NotBlank(message = "Nome é obrigatório") String name,
    String logoUrl,
    boolean isOpen,
    String openingHours,
    String address
) {}

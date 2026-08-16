package com.fidecode.biteflow.application.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record DriverRequest(
    @NotBlank(message = "name é obrigatório") String name,
    String phone,
    String status,
    Boolean isFixed,
    Boolean isDaily,
    BigDecimal allowance,
    Boolean hasFixedDeliveryValue,
    BigDecimal fixedDeliveryValue
) {}

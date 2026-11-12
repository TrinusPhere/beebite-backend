package com.fidecode.beebite.application.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        String orderId,
        String cashSessionId,
        String driverId,
        @NotBlank(message = "method é obrigatório") String method,
        @NotNull(message = "amount é obrigatório")
        @DecimalMin(value = "0.01", message = "amount deve ser maior que zero") BigDecimal amount,
        BigDecimal changeFor,
        String collectedBy, // 'CAIXA', 'ENTREGADOR', 'ONLINE'
        String notes
) {}

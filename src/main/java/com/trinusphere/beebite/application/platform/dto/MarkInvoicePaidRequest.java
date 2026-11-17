package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkInvoicePaidRequest(
        @NotBlank(message = "O método de pagamento é obrigatório.") String paymentMethod,
        String notes
) {
}


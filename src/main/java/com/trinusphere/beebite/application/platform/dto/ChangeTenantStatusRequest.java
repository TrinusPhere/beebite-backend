package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeTenantStatusRequest(
        @NotBlank(message = "O status é obrigatório.") String status
) {
}


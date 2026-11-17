package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTenantAccountRequest(
        @NotBlank(message = "A razão social é obrigatória.") String companyName,
        @NotBlank(message = "O nome fantasia é obrigatório.") String tradeName,
        @NotBlank(message = "O nome do proprietário é obrigatório.") String ownerName,
        String documentNumber,
        String email,
        String phone,
        String notes
) {
}


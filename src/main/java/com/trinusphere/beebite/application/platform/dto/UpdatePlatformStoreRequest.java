package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePlatformStoreRequest(
        @NotBlank(message = "O nome da loja é obrigatório.") String name,
        String cnpj,
        String address,
        String publicSlug,
        @NotBlank(message = "O modo de sincronização é obrigatório.") String syncMode,
        boolean active
) {
}


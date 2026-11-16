package com.trinusphere.beebite.application.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record GrantUserStoreAccessRequest(
        @NotBlank(message = "userId é obrigatório.") String userId,
        @NotBlank(message = "storeId é obrigatório.") String storeId,
        @NotBlank(message = "role é obrigatório.") String role,
        List<String> permissions
) {
}

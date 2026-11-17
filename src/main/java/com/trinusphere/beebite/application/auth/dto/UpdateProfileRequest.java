package com.trinusphere.beebite.application.auth.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.") String name,
        String currentPassword,
        @Size(min = 6, max = 100, message = "A nova senha deve ter no mínimo 6 caracteres.") String newPassword
) {
}


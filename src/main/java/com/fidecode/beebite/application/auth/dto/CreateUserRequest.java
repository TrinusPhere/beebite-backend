package com.fidecode.beebite.application.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateUserRequest(@NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Login é obrigatório") String login,
        @NotBlank(message = "Senha é obrigatória") String password,
        @NotNull(message = "Role é obrigatório") String role, List<String> permissions) {
}

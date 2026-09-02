package com.fidecode.biteflow.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CategoryRequest(@NotBlank(message = "Nome é obrigatório") String name, String status, Integer displayOrder,
        List<String> defaultModifierGroupIds) {
}

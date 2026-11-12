package com.fidecode.beebite.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record ModifierGroupRequest(@NotBlank(message = "Nome é obrigatório") String name, String type, Boolean required,
        Integer minSelections, Integer maxSelections, String icon, String semanticType, List<ModifierOptionDto> options) {
    public record ModifierOptionDto(String name, BigDecimal price, String linkedProductId) {
    }
}

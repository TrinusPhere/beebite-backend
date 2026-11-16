package com.trinusphere.beebite.application.printing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Preview ao vivo: {@code template} opcional (se ausente, usa o modelo salvo); {@code paperWidthMm} define a largura
 * simulada.
 */
public record PreviewReceiptRequest(@Valid PrintTemplateRequest template, @NotNull @Min(58) @Max(80) Integer paperWidthMm) {
}


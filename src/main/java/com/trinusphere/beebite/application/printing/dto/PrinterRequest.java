package com.trinusphere.beebite.application.printing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Campos de hardware são opcionais; ausentes mantêm os padrões (80mm, porta 9100, CP860, com corte). */
public record PrinterRequest(@NotBlank @Size(max = 120) String name, @NotBlank String connectionType,
        @Size(max = 255) String address, Boolean active, @Min(58) @Max(80) Integer paperWidthMm,
        @Min(1) @Max(65535) Integer port, String codePage, Boolean cutPaper) {
}

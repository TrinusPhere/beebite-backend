package com.fidecode.biteflow.application.kds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record KdsSettingsRequest(
        @NotBlank(message = "dispatchMode é obrigatório") String dispatchMode,
        @Min(value = 1, message = "warningAlertMinutes deve ser maior que zero") int warningAlertMinutes,
        @Min(value = 1, message = "criticalAlertMinutes deve ser maior que zero") int criticalAlertMinutes,
        boolean autoCompleteOnExpedition
) {}


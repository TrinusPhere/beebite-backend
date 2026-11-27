package com.trinusphere.beebite.application.sync.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TriggerReconciliationRequest(
        @NotNull LocalDate from,
        @NotNull LocalDate to
) {
}


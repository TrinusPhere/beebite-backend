package com.fidecode.biteflow.application.order.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AssignDriverRequest(@NotBlank(message = "driverId é obrigatório") String driverId, String driverName,
        List<String> orderIds) {
}

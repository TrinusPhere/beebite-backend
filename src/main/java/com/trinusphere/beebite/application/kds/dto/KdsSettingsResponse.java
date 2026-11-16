package com.trinusphere.beebite.application.kds.dto;

import java.time.LocalDateTime;

public record KdsSettingsResponse(
        String storeId,
        String dispatchMode,
        int warningAlertMinutes,
        int criticalAlertMinutes,
        boolean autoCompleteOnExpedition,
        LocalDateTime updatedAt
) {}


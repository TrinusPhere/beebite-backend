package com.fidecode.biteflow.application.kds.dto;

import com.fidecode.biteflow.domain.kds.entity.KdsSettings;

public final class KdsSettingsDtoMapper {

    private KdsSettingsDtoMapper() {}

    public static KdsSettingsResponse toResponse(KdsSettings s) {
        if (s == null) return null;
        return new KdsSettingsResponse(
                s.getStoreId() != null ? s.getStoreId().toString() : null,
                s.getDispatchMode().name(),
                s.getWarningAlertMinutes(),
                s.getCriticalAlertMinutes(),
                s.isAutoCompleteOnExpedition(),
                s.getUpdatedAt()
        );
    }
}


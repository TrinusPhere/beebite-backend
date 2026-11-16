package com.trinusphere.beebite.application.kds.dto;

import com.trinusphere.beebite.domain.kds.entity.KdsSettings;

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


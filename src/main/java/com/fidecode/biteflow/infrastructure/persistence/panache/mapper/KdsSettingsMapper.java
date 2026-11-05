package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.KdsSettingsEntity;

public final class KdsSettingsMapper {

    private KdsSettingsMapper() {}

    public static KdsSettings toDomain(KdsSettingsEntity entity) {
        if (entity == null) return null;
        DispatchMode mode = entity.dispatchMode != null ? DispatchMode.valueOf(entity.dispatchMode) : DispatchMode.AUTO;
        return KdsSettings.reconstitute(
                entity.storeId,
                mode,
                entity.warningAlertMinutes,
                entity.criticalAlertMinutes,
                entity.autoCompleteOnExpedition,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public static KdsSettingsEntity toEntity(KdsSettings domain) {
        if (domain == null) return null;
        KdsSettingsEntity entity = new KdsSettingsEntity();
        entity.storeId = domain.getStoreId();
        entity.dispatchMode = domain.getDispatchMode() != null ? domain.getDispatchMode().name() : DispatchMode.AUTO.name();
        entity.warningAlertMinutes = domain.getWarningAlertMinutes();
        entity.criticalAlertMinutes = domain.getCriticalAlertMinutes();
        entity.autoCompleteOnExpedition = domain.isAutoCompleteOnExpedition();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }
}

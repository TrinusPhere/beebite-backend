package com.fidecode.biteflow.application.kds;

import com.fidecode.biteflow.application.kds.dto.KdsSettingsDtoMapper;
import com.fidecode.biteflow.application.kds.dto.KdsSettingsRequest;
import com.fidecode.biteflow.application.kds.dto.KdsSettingsResponse;
import com.fidecode.biteflow.domain.kds.entity.KdsSettings;
import com.fidecode.biteflow.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.biteflow.domain.kds.valueobject.DispatchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateKdsSettingsUseCase {

    private final KdsSettingsRepository settingsRepository;

    @Inject
    public UpdateKdsSettingsUseCase(KdsSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public KdsSettingsResponse execute(UUID storeId, KdsSettingsRequest request) {
        KdsSettings settings = settingsRepository.findByStoreId(storeId)
                .orElseGet(() -> KdsSettings.createDefault(storeId));

        DispatchMode mode = DispatchMode.valueOf(request.dispatchMode().toUpperCase());
        settings.update(mode, request.warningAlertMinutes(), request.criticalAlertMinutes(), request.autoCompleteOnExpedition());

        KdsSettings saved = settingsRepository.save(settings);
        return KdsSettingsDtoMapper.toResponse(saved);
    }
}


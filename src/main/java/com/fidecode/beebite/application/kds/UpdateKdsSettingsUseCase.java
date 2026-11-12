package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.application.kds.dto.KdsSettingsDtoMapper;
import com.fidecode.beebite.application.kds.dto.KdsSettingsRequest;
import com.fidecode.beebite.application.kds.dto.KdsSettingsResponse;
import com.fidecode.beebite.domain.kds.entity.KdsSettings;
import com.fidecode.beebite.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.beebite.domain.kds.valueobject.DispatchMode;
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


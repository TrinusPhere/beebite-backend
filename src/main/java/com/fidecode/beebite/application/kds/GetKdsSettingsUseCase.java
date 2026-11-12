package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.application.kds.dto.KdsSettingsDtoMapper;
import com.fidecode.beebite.application.kds.dto.KdsSettingsResponse;
import com.fidecode.beebite.domain.kds.entity.KdsSettings;
import com.fidecode.beebite.domain.kds.repository.KdsSettingsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetKdsSettingsUseCase {

    private final KdsSettingsRepository settingsRepository;

    @Inject
    public GetKdsSettingsUseCase(KdsSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public KdsSettingsResponse execute(UUID storeId) {
        KdsSettings settings = settingsRepository.findByStoreId(storeId)
                .orElseGet(() -> KdsSettings.createDefault(storeId));
        return KdsSettingsDtoMapper.toResponse(settings);
    }
}


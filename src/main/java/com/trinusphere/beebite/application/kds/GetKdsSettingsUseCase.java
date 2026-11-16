package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.KdsSettingsDtoMapper;
import com.trinusphere.beebite.application.kds.dto.KdsSettingsResponse;
import com.trinusphere.beebite.domain.kds.entity.KdsSettings;
import com.trinusphere.beebite.domain.kds.repository.KdsSettingsRepository;
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


package com.trinusphere.beebite.domain.kds.repository;

import com.trinusphere.beebite.domain.kds.entity.KdsSettings;

import java.util.Optional;
import java.util.UUID;

public interface KdsSettingsRepository {
    KdsSettings save(KdsSettings settings);
    Optional<KdsSettings> findByStoreId(UUID storeId);
}


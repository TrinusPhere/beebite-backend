package com.fidecode.biteflow.domain.kds.repository;

import com.fidecode.biteflow.domain.kds.entity.KdsStation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KdsStationRepository {
    KdsStation save(KdsStation station);
    Optional<KdsStation> findById(UUID id);
    List<KdsStation> findAllByStoreId(UUID storeId);
    List<KdsStation> findActiveByStoreId(UUID storeId);
    void delete(KdsStation station);
}


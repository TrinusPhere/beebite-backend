package com.fidecode.biteflow.domain.integration.repository;

import com.fidecode.biteflow.domain.integration.entity.IntegrationConfig;
import java.util.*;

public interface IntegrationConfigRepository {
    Optional<IntegrationConfig> findByStoreIdAndProvider(UUID storeId, String provider);

    List<IntegrationConfig> findAllByStoreId(UUID storeId);

    IntegrationConfig save(IntegrationConfig config);
}

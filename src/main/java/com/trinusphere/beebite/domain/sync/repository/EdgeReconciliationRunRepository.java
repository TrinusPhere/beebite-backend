package com.trinusphere.beebite.domain.sync.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;

import java.util.Optional;
import java.util.UUID;

public interface EdgeReconciliationRunRepository {
    EdgeReconciliationRun save(EdgeReconciliationRun run);

    Optional<EdgeReconciliationRun> findLatestByStoreId(UUID storeId);
}


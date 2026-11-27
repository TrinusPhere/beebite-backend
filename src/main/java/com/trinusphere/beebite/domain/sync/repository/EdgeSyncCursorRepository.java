package com.trinusphere.beebite.domain.sync.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;

import java.util.Optional;
import java.util.UUID;

public interface EdgeSyncCursorRepository {
    Optional<EdgeSyncCursor> findByStoreId(UUID storeId);

    EdgeSyncCursor save(EdgeSyncCursor cursor);
}


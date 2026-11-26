package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;

import java.time.Instant;
import java.util.UUID;

public interface EdgeCatalogSyncApplier {
    EdgeCatalogDeltaResponse buildDeltaForStore(UUID storeId, Instant since);

    int applyDeltaLocally(UUID edgeStoreId, EdgeCatalogDeltaResponse delta);
}


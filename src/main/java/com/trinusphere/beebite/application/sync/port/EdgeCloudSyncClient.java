package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;

import java.time.Instant;
import java.util.UUID;

public interface EdgeCloudSyncClient {
    EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken);

    EdgeCatalogDeltaResponse pullCatalogDelta(UUID storeId, Instant since, String deviceToken);
}

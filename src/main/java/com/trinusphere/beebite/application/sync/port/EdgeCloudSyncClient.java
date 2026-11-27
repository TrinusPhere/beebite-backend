package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;

import java.time.Instant;
import java.util.UUID;

public interface EdgeCloudSyncClient {
    EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken);

    EdgeCatalogDeltaResponse pullCatalogDelta(UUID storeId, Instant since, String deviceToken);

    EdgeReconcileBucketsResponse compareBuckets(EdgeReconcileBucketsRequest request, String deviceToken);

    EdgeReconcileItemsResponse compareItems(EdgeReconcileItemsRequest request, String deviceToken);
}

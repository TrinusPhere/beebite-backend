package com.fidecode.biteflow.application.sync.port;

import com.fidecode.biteflow.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.biteflow.application.sync.dto.EdgeSyncBatchResponse;

public interface EdgeCloudSyncClient {
    EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken);
}

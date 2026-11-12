package com.fidecode.beebite.application.sync.port;

import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.beebite.application.sync.dto.EdgeSyncBatchResponse;

public interface EdgeCloudSyncClient {
    EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken);
}

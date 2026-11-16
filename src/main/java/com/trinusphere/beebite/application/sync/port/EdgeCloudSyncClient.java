package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;

public interface EdgeCloudSyncClient {
    EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken);
}

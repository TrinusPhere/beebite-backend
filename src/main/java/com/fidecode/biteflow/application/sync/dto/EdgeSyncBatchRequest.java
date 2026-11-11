package com.fidecode.biteflow.application.sync.dto;

import java.util.List;
import java.util.UUID;

public record EdgeSyncBatchRequest(
        UUID storeId,
        List<EdgeSyncEventDto> events
) {
}

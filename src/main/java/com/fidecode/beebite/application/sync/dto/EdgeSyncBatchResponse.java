package com.fidecode.beebite.application.sync.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EdgeSyncBatchResponse(
        int processedCount,
        List<UUID> ackedEventIds,
        Instant serverTimestamp
) {
}

package com.trinusphere.beebite.application.sync.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EdgeSyncBatchResponse(
        int processedCount,
        List<UUID> ackedEventIds,
        List<EdgeSyncRejectedEventDto> rejectedEvents,
        Long lastContiguousSequence,
        boolean requiresFullResync,
        Instant serverTimestamp
) {
    public EdgeSyncBatchResponse(
            int processedCount,
            List<UUID> ackedEventIds,
            Instant serverTimestamp) {
        this(processedCount, ackedEventIds, List.of(), null, false, serverTimestamp);
    }
}

package com.fidecode.biteflow.application.sync.dto;

import java.time.Instant;
import java.util.UUID;

public record EdgeSyncEventDto(
        UUID eventId,
        String eventType,
        UUID aggregateId,
        String payloadJson,
        Instant occurredAt
) {
}

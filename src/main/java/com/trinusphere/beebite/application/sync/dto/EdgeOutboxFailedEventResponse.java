package com.trinusphere.beebite.application.sync.dto;

import java.time.Instant;
import java.util.UUID;

public record EdgeOutboxFailedEventResponse(
        UUID id,
        Long sequence,
        String eventType,
        UUID aggregateId,
        int attempts,
        String rejectedReason,
        Instant createdAt
) {
}


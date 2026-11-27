package com.trinusphere.beebite.application.sync.dto;

import java.time.Instant;
import java.util.UUID;

public record EdgeSyncEventDto(
        UUID eventId,
        String eventType,
        UUID aggregateId,
        String payloadJson,
        Instant occurredAt,
        Long sequence
) {
    public EdgeSyncEventDto(
            UUID eventId,
            String eventType,
            UUID aggregateId,
            String payloadJson,
            Instant occurredAt) {
        this(eventId, eventType, aggregateId, payloadJson, occurredAt, null);
    }
}

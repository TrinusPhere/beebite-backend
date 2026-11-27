package com.trinusphere.beebite.application.sync.dto;

import java.util.UUID;

public record EdgeSyncRejectedEventDto(UUID eventId, String reason) {
}


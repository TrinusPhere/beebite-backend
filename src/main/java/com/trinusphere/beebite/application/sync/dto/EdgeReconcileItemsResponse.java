package com.trinusphere.beebite.application.sync.dto;

import java.util.List;
import java.util.UUID;

public record EdgeReconcileItemsResponse(
        List<UUID> missingInCloud,
        List<UUID> differentInCloud,
        List<UUID> extraInCloud
) {
}


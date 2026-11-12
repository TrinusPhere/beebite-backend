package com.fidecode.beebite.infrastructure.kds;

import java.util.UUID;

public record KdsEventPayload(
        UUID storeId,
        String eventType, // "TASKS_DISPATCHED", "TASK_UPDATED", "TASKS_CANCELED"
        Object data
) {}

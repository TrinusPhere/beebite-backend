package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;

import java.util.List;
import java.util.UUID;

public interface EdgeOutboxRequeuePort {
    int requeue(UUID storeId, ReconciliationEntityType type, List<UUID> aggregateIds);

    int requeueByEventType(UUID storeId, String eventType, UUID aggregateId);
}


package com.fidecode.beebite.application.sync.port;

import java.util.UUID;

public interface EdgeSnapshotApplier {
    void apply(UUID storeId, String eventType, UUID aggregateId, String payloadJson);
}

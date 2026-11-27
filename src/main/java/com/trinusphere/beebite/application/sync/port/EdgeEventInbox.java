package com.trinusphere.beebite.application.sync.port;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;

import java.util.UUID;

public interface EdgeEventInbox {
    EdgeEventOutcome applyInOrder(UUID storeId, EdgeSyncEventDto event);

    EdgeEventOutcome applyLegacy(UUID storeId, EdgeSyncEventDto event);
}


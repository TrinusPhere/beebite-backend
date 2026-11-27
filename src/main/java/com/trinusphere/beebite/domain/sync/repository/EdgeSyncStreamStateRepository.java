package com.trinusphere.beebite.domain.sync.repository;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;

import java.util.UUID;

public interface EdgeSyncStreamStateRepository {
    EdgeSyncStreamState findOrInitialize(UUID storeId);

    EdgeSyncStreamState save(EdgeSyncStreamState state);
}


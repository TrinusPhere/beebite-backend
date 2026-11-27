package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.sync.repository.EdgeSyncedEventLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;

@ApplicationScoped
public class PanacheEdgeSyncedEventLogRepository implements EdgeSyncedEventLogRepository {

    @Inject
    EdgeSyncedEventLogPanacheRepo repo;

    @Override
    @Transactional
    public int purgeReceivedBefore(Instant cutoff) {
        return (int) repo.delete("receivedAt < ?1", cutoff);
    }
}


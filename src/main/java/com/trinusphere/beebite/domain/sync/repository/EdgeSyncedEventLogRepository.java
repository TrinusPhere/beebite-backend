package com.trinusphere.beebite.domain.sync.repository;

import java.time.Instant;

public interface EdgeSyncedEventLogRepository {
    int purgeReceivedBefore(Instant cutoff);
}


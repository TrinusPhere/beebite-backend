package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncedEventLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class PurgeEdgeSyncHistoryUseCase {

    private final EdgeOutboxRepository outboxRepository;
    private final EdgeSyncedEventLogRepository eventLogRepository;
    private final String syncMode;
    private final int outboxRetentionDays;
    private final int eventLogRetentionDays;

    @Inject
    public PurgeEdgeSyncHistoryUseCase(
            EdgeOutboxRepository outboxRepository,
            EdgeSyncedEventLogRepository eventLogRepository,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.sync.outbox-retention-days", defaultValue = "30") int outboxRetentionDays,
            @ConfigProperty(name = "biteflow.sync.event-log-retention-days", defaultValue = "90") int eventLogRetentionDays) {
        this.outboxRepository = outboxRepository;
        this.eventLogRepository = eventLogRepository;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.outboxRetentionDays = Math.max(1, outboxRetentionDays);
        this.eventLogRetentionDays = Math.max(1, eventLogRetentionDays);
    }

    public int execute() {
        Instant now = Instant.now();
        if ("EDGE".equals(syncMode)) {
            return outboxRepository.purgeSyncedBefore(now.minus(Duration.ofDays(outboxRetentionDays)));
        }
        return eventLogRepository.purgeReceivedBefore(now.minus(Duration.ofDays(eventLogRetentionDays)));
    }
}


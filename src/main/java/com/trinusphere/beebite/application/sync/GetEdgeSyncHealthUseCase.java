package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncHealthResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationResult;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeReconciliationRunRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class GetEdgeSyncHealthUseCase {

    private final EdgeOutboxRepository outboxRepository;
    private final EdgeSyncStreamStateRepository streamStateRepository;
    private final EdgeReconciliationRunRepository runRepository;
    private final String syncMode;

    @Inject
    public GetEdgeSyncHealthUseCase(
            EdgeOutboxRepository outboxRepository,
            EdgeSyncStreamStateRepository streamStateRepository,
            EdgeReconciliationRunRepository runRepository,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode) {
        this.outboxRepository = outboxRepository;
        this.streamStateRepository = streamStateRepository;
        this.runRepository = runRepository;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
    }

    public EdgeSyncHealthResponse execute(UUID storeId) {
        if (!"EDGE".equals(syncMode)) {
            return new EdgeSyncHealthResponse(
                    syncMode, 0L, 0L, null, 0L, null, null, 0, null, false, null
            );
        }

        long pending = outboxRepository.countByStatus(storeId, EdgeOutboxStatus.PENDING);
        long failed = outboxRepository.countByStatus(storeId, EdgeOutboxStatus.FAILED);
        Instant now = Instant.now();
        Long oldestPendingAgeSeconds = outboxRepository.findOldestPendingCreatedAt(storeId)
                .map(created -> Math.max(0L, Duration.between(created, now).getSeconds()))
                .orElse(null);

        EdgeSyncStreamState state = streamStateRepository.findOrInitialize(storeId);
        ReconciliationResult lastReconciliation = runRepository.findLatestByStoreId(storeId)
                .map(EdgeSyncDtoMapper::toReconciliationResult)
                .orElse(null);

        return new EdgeSyncHealthResponse(
                syncMode,
                pending,
                failed,
                oldestPendingAgeSeconds,
                state.getCloudAckSequence(),
                state.getLastSuccessAt(),
                state.getNextAttemptAt(),
                state.getConsecutiveFailures(),
                state.getLastError(),
                state.isFullResyncRequested(),
                lastReconciliation
        );
    }
}


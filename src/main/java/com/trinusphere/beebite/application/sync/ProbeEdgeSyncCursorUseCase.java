package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProbeEdgeSyncCursorUseCase {

    private final EdgeOutboxRepository outboxRepository;
    private final EdgeSyncStreamStateRepository streamStateRepository;
    private final EdgeCloudSyncClient cloudClient;
    private final EdgeSyncAckHandler ackHandler;
    private final String syncMode;
    private final String edgeStoreId;
    private final String deviceToken;
    private final Duration pollInterval;

    @Inject
    public ProbeEdgeSyncCursorUseCase(
            EdgeOutboxRepository outboxRepository,
            EdgeSyncStreamStateRepository streamStateRepository,
            EdgeCloudSyncClient cloudClient,
            EdgeSyncAckHandler ackHandler,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreId,
            @ConfigProperty(name = "biteflow.sync.edge-device-token", defaultValue = "") String deviceToken,
            @ConfigProperty(name = "biteflow.sync.poll-interval", defaultValue = "15s") Duration pollInterval) {
        this.outboxRepository = outboxRepository;
        this.streamStateRepository = streamStateRepository;
        this.cloudClient = cloudClient;
        this.ackHandler = ackHandler;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.edgeStoreId = edgeStoreId != null ? edgeStoreId.orElse("").trim() : "";
        this.deviceToken = deviceToken != null ? deviceToken.trim() : "";
        this.pollInterval = pollInterval != null ? pollInterval : Duration.ofSeconds(15);
    }

    public boolean execute() {
        if (!"EDGE".equals(syncMode) || edgeStoreId.isBlank()) {
            return false;
        }
        UUID storeId;
        try {
            storeId = UUID.fromString(edgeStoreId);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        Instant now = Instant.now();
        EdgeSyncStreamState state = streamStateRepository.findOrInitialize(storeId);
        if (!state.canAttempt(now)) {
            return false;
        }

        Long firstAvailable = outboxRepository.findMinRetainedSequence(storeId).orElse(null);
        try {
            EdgeSyncBatchResponse response = cloudClient.sendBatch(
                    new EdgeSyncBatchRequest(storeId, List.of(), firstAvailable),
                    deviceToken
            );
            int reopened = ackHandler.handle(storeId, state, response, now);
            return reopened > 0;
        } catch (Exception ex) {
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            state.recordFailure(errorMsg, now, pollInterval);
            streamStateRepository.save(state);
            return false;
        }
    }
}


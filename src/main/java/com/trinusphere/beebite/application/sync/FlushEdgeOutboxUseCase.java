package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncRejectedEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class FlushEdgeOutboxUseCase {

    private static final Logger LOG = Logger.getLogger(FlushEdgeOutboxUseCase.class.getName());
    private static final int BATCH_LIMIT = 50;

    private final EdgeOutboxRepository edgeOutboxRepository;
    private final EdgeSyncStreamStateRepository streamStateRepository;
    private final EdgeCloudSyncClient edgeCloudSyncClient;
    private final EdgeSyncAckHandler ackHandler;
    private final String syncMode;
    private final String deviceToken;
    private final Duration pollInterval;

    @Inject
    public FlushEdgeOutboxUseCase(
            EdgeOutboxRepository edgeOutboxRepository,
            EdgeSyncStreamStateRepository streamStateRepository,
            EdgeCloudSyncClient edgeCloudSyncClient,
            EdgeSyncAckHandler ackHandler,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.sync.edge-device-token", defaultValue = "") String deviceToken,
            @ConfigProperty(name = "biteflow.sync.poll-interval", defaultValue = "15s") Duration pollInterval) {
        this.edgeOutboxRepository = edgeOutboxRepository;
        this.streamStateRepository = streamStateRepository;
        this.edgeCloudSyncClient = edgeCloudSyncClient;
        this.ackHandler = ackHandler;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.deviceToken = deviceToken != null ? deviceToken.trim() : "";
        this.pollInterval = pollInterval != null ? pollInterval : Duration.ofSeconds(15);
    }

    public int execute() {
        if (!"EDGE".equals(syncMode)) {
            return 0;
        }

        List<UUID> storeIds = edgeOutboxRepository.findStoreIdsWithPending();
        if (storeIds.isEmpty()) {
            return 0;
        }

        int totalSynced = 0;

        for (UUID storeId : storeIds) {
            Instant now = Instant.now();
            EdgeSyncStreamState state = streamStateRepository.findOrInitialize(storeId);
            if (!state.canAttempt(now)) {
                continue;
            }

            List<EdgeOutboxEvent> storeEvents = edgeOutboxRepository.findPendingBatch(storeId, BATCH_LIMIT);
            if (storeEvents.isEmpty()) {
                continue;
            }

            Long firstAvailable = edgeOutboxRepository.findMinRetainedSequence(storeId).orElse(null);
            List<EdgeSyncEventDto> dtos = storeEvents.stream()
                    .map(e -> new EdgeSyncEventDto(
                            e.getId(),
                            e.getEventType(),
                            e.getAggregateId(),
                            e.getPayloadJson(),
                            e.getCreatedAt(),
                            e.getSequence()
                    ))
                    .toList();

            List<UUID> sentIds = storeEvents.stream()
                    .map(EdgeOutboxEvent::getId)
                    .filter(id -> id != null)
                    .toList();

            try {
                EdgeSyncBatchResponse response = edgeCloudSyncClient.sendBatch(
                        new EdgeSyncBatchRequest(storeId, dtos, firstAvailable),
                        deviceToken
                );

                Instant syncedAt = (response != null && response.serverTimestamp() != null)
                        ? response.serverTimestamp()
                        : now;

                Set<UUID> handledIds = new HashSet<>();

                if (response != null && response.ackedEventIds() != null && !response.ackedEventIds().isEmpty()) {
                    edgeOutboxRepository.markSynced(response.ackedEventIds(), syncedAt);
                    handledIds.addAll(response.ackedEventIds());
                    totalSynced += response.ackedEventIds().size();
                }

                if (response != null && response.rejectedEvents() != null) {
                    for (EdgeSyncRejectedEventDto rej : response.rejectedEvents()) {
                        if (rej != null && rej.eventId() != null) {
                            edgeOutboxRepository.markRejected(rej.eventId(), rej.reason());
                            handledIds.add(rej.eventId());
                            LOG.warning(() -> String.format(
                                    "Evento Edge %s rejeitado pela nuvem: %s", rej.eventId(), rej.reason()));
                        }
                    }
                }

                List<UUID> unackedIds = sentIds.stream()
                        .filter(id -> !handledIds.contains(id))
                        .toList();
                if (!unackedIds.isEmpty()) {
                    edgeOutboxRepository.recordTransientFailure(unackedIds, "Não confirmado pela nuvem neste lote");
                }

                ackHandler.handle(storeId, state, response, now);
            } catch (Exception ex) {
                String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                if (!sentIds.isEmpty()) {
                    edgeOutboxRepository.recordTransientFailure(sentIds, errorMsg);
                }
                state.recordFailure(errorMsg, now, pollInterval);
                streamStateRepository.save(state);
            }
        }

        return totalSynced;
    }
}

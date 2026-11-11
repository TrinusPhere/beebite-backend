package com.fidecode.biteflow.application.sync;

import com.fidecode.biteflow.application.sync.dto.EdgeSyncBatchRequest;
import com.fidecode.biteflow.application.sync.dto.EdgeSyncBatchResponse;
import com.fidecode.biteflow.application.sync.dto.EdgeSyncEventDto;
import com.fidecode.biteflow.application.sync.port.EdgeCloudSyncClient;
import com.fidecode.biteflow.domain.sync.entity.EdgeOutboxEvent;
import com.fidecode.biteflow.domain.sync.repository.EdgeOutboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FlushEdgeOutboxUseCase {

    private static final int BATCH_LIMIT = 50;

    private final EdgeOutboxRepository edgeOutboxRepository;
    private final EdgeCloudSyncClient edgeCloudSyncClient;
    private final String syncMode;
    private final String deviceToken;

    @Inject
    public FlushEdgeOutboxUseCase(
            EdgeOutboxRepository edgeOutboxRepository,
            EdgeCloudSyncClient edgeCloudSyncClient,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.sync.edge-device-token", defaultValue = "") String deviceToken) {
        this.edgeOutboxRepository = edgeOutboxRepository;
        this.edgeCloudSyncClient = edgeCloudSyncClient;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.deviceToken = deviceToken != null ? deviceToken.trim() : "";
    }

    public int execute() {
        if (!"EDGE".equals(syncMode)) {
            return 0;
        }

        List<EdgeOutboxEvent> pending = edgeOutboxRepository.findPendingBatch(BATCH_LIMIT);
        if (pending.isEmpty()) {
            return 0;
        }

        Map<UUID, List<EdgeOutboxEvent>> byStore = pending.stream()
                .collect(Collectors.groupingBy(EdgeOutboxEvent::getStoreId));

        int totalSynced = 0;

        for (Map.Entry<UUID, List<EdgeOutboxEvent>> entry : byStore.entrySet()) {
            UUID storeId = entry.getKey();
            List<EdgeOutboxEvent> storeEvents = entry.getValue();

            List<EdgeSyncEventDto> dtos = storeEvents.stream()
                    .map(e -> new EdgeSyncEventDto(
                            e.getId(),
                            e.getEventType(),
                            e.getAggregateId(),
                            e.getPayloadJson(),
                            e.getCreatedAt()
                    ))
                    .toList();

            try {
                EdgeSyncBatchResponse response = edgeCloudSyncClient.sendBatch(
                        new EdgeSyncBatchRequest(storeId, dtos),
                        deviceToken
                );
                if (response != null && response.ackedEventIds() != null && !response.ackedEventIds().isEmpty()) {
                    Instant syncedAt = response.serverTimestamp() != null ? response.serverTimestamp() : Instant.now();
                    edgeOutboxRepository.markSynced(response.ackedEventIds(), syncedAt);
                    totalSynced += response.ackedEventIds().size();
                }
            } catch (Exception ex) {
                String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                for (EdgeOutboxEvent failedEvent : storeEvents) {
                    if (failedEvent.getId() != null) {
                        edgeOutboxRepository.markFailed(failedEvent.getId(), errorMsg);
                    }
                }
            }
        }

        return totalSynced;
    }
}

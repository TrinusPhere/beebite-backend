package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.port.EdgeOutboxBackfillPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BackfillEdgeOutboxUseCase {

    private final EdgeOutboxBackfillPort backfillPort;
    private final String syncMode;
    private final String edgeStoreId;

    @Inject
    public BackfillEdgeOutboxUseCase(
            EdgeOutboxBackfillPort backfillPort,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreId) {
        this.backfillPort = backfillPort;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.edgeStoreId = edgeStoreId != null ? edgeStoreId.orElse("").trim() : "";
    }

    public int execute() {
        if (!"EDGE".equals(syncMode) || edgeStoreId.isBlank()) {
            return 0;
        }
        try {
            UUID storeId = UUID.fromString(edgeStoreId);
            return backfillPort.backfillMissingEvents(storeId);
        } catch (IllegalArgumentException ex) {
            return 0;
        }
    }
}


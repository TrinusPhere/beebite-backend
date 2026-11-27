package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RunRequestedFullResyncUseCase {

    private static final int FULL_RESYNC_DAYS = 30;

    private final EdgeSyncStreamStateRepository streamStateRepository;
    private final ReconcileEdgeDataUseCase reconcileEdgeDataUseCase;
    private final String syncMode;
    private final String edgeStoreId;

    @Inject
    public RunRequestedFullResyncUseCase(
            EdgeSyncStreamStateRepository streamStateRepository,
            ReconcileEdgeDataUseCase reconcileEdgeDataUseCase,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreId) {
        this.streamStateRepository = streamStateRepository;
        this.reconcileEdgeDataUseCase = reconcileEdgeDataUseCase;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.edgeStoreId = edgeStoreId != null ? edgeStoreId.orElse("").trim() : "";
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

        EdgeSyncStreamState state = streamStateRepository.findOrInitialize(storeId);
        if (!state.isFullResyncRequested()) {
            return false;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return reconcileEdgeDataUseCase.execute(
                ReconciliationTrigger.FULL_RESYNC,
                today.minusDays(FULL_RESYNC_DAYS),
                today
        ).isPresent();
    }
}


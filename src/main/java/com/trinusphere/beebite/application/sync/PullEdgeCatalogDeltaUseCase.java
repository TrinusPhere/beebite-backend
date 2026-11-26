package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCatalogSyncApplier;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PullEdgeCatalogDeltaUseCase {

    private static final Logger LOG = Logger.getLogger(PullEdgeCatalogDeltaUseCase.class);

    private final StoreRepository storeRepository;
    private final EdgeCloudSyncClient edgeCloudSyncClient;
    private final EdgeCatalogSyncApplier edgeCatalogSyncApplier;
    private final String syncMode;
    private final Optional<String> edgeStoreIdRaw;
    private final String deviceToken;

    @Inject
    public PullEdgeCatalogDeltaUseCase(
            StoreRepository storeRepository,
            EdgeCloudSyncClient edgeCloudSyncClient,
            EdgeCatalogSyncApplier edgeCatalogSyncApplier,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreIdRaw,
            @ConfigProperty(name = "biteflow.sync.edge-device-token", defaultValue = "none") String deviceToken) {
        this.storeRepository = storeRepository;
        this.edgeCloudSyncClient = edgeCloudSyncClient;
        this.edgeCatalogSyncApplier = edgeCatalogSyncApplier;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase(Locale.ROOT) : "CLOUD";
        this.edgeStoreIdRaw = edgeStoreIdRaw != null ? edgeStoreIdRaw : Optional.empty();
        this.deviceToken = deviceToken != null ? deviceToken.trim() : "";
    }

    public int execute() {
        if (!"EDGE".equals(syncMode) || deviceToken.isBlank() || "none".equalsIgnoreCase(deviceToken)) {
            return 0;
        }

        UUID edgeStoreId = resolveEdgeStoreId();
        if (edgeStoreId == null) {
            return 0;
        }

        Optional<Store> storeOpt = storeRepository.findById(edgeStoreId);
        if (storeOpt.isEmpty()) {
            return 0;
        }

        Store store = storeOpt.get();
        Instant since = store.getLastCatalogPullAt();

        try {
            EdgeCatalogDeltaResponse delta = edgeCloudSyncClient.pullCatalogDelta(edgeStoreId, since, deviceToken);
            if (delta == null) {
                return 0;
            }
            int applied = edgeCatalogSyncApplier.applyDeltaLocally(edgeStoreId, delta);
            Instant nextCursor = delta.serverTimestamp() != null ? delta.serverTimestamp() : Instant.now();
            store.markCatalogPulled(nextCursor);
            if (delta.catalogId() != null) {
                store.assignCatalog(delta.catalogId());
            }
            storeRepository.save(store);
            return applied;
        } catch (Exception ex) {
            LOG.debugf("[EDGE-CATALOG-PULL] Nuvem inacessível ou falha temporária no pull de cardápio: %s", ex.getMessage());
            return 0;
        }
    }

    private UUID resolveEdgeStoreId() {
        if (edgeStoreIdRaw.isPresent() && !edgeStoreIdRaw.get().isBlank()) {
            try {
                return UUID.fromString(edgeStoreIdRaw.get().trim());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return storeRepository.findAll().stream()
                .filter(s -> "EDGE".equalsIgnoreCase(s.getSyncMode()))
                .map(Store::getId)
                .findFirst()
                .orElse(null);
    }
}

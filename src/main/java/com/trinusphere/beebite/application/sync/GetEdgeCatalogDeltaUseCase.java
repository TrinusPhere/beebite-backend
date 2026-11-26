package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCatalogSyncApplier;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.exception.InvalidEdgeDeviceTokenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class GetEdgeCatalogDeltaUseCase {

    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EdgeCatalogSyncApplier edgeCatalogSyncApplier;

    @Inject
    public GetEdgeCatalogDeltaUseCase(
            StoreRepository storeRepository,
            PasswordEncoder passwordEncoder,
            EdgeCatalogSyncApplier edgeCatalogSyncApplier) {
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
        this.edgeCatalogSyncApplier = edgeCatalogSyncApplier;
    }

    public EdgeCatalogDeltaResponse execute(String deviceToken, UUID storeId, Instant since) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório para sincronização de cardápio.");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        validateDeviceToken(deviceToken, store);

        return edgeCatalogSyncApplier.buildDeltaForStore(storeId, since);
    }

    private void validateDeviceToken(String deviceToken, Store store) {
        String configuredHash = store.getEdgeApiKeyHash();
        if (deviceToken == null || deviceToken.isBlank() || configuredHash == null || configuredHash.isBlank()) {
            throw new InvalidEdgeDeviceTokenException("Token de dispositivo Edge ausente ou não configurado para esta loja.");
        }

        boolean valid;
        if (configuredHash.startsWith("$2a$") || configuredHash.startsWith("$2b$") || configuredHash.startsWith("$2y$")) {
            valid = passwordEncoder.matches(deviceToken, configuredHash);
        } else {
            valid = MessageDigest.isEqual(
                    deviceToken.getBytes(StandardCharsets.UTF_8),
                    configuredHash.getBytes(StandardCharsets.UTF_8)
            );
        }

        if (!valid) {
            throw new InvalidEdgeDeviceTokenException("Token de dispositivo Edge inválido.");
        }
    }
}


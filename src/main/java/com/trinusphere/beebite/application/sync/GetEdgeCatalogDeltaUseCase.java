package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCatalogSyncApplier;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class GetEdgeCatalogDeltaUseCase {

    private final StoreRepository storeRepository;
    private final EdgeDeviceTokenValidator tokenValidator;
    private final EdgeCatalogSyncApplier edgeCatalogSyncApplier;

    @Inject
    public GetEdgeCatalogDeltaUseCase(
            StoreRepository storeRepository,
            EdgeDeviceTokenValidator tokenValidator,
            EdgeCatalogSyncApplier edgeCatalogSyncApplier) {
        this.storeRepository = storeRepository;
        this.tokenValidator = tokenValidator;
        this.edgeCatalogSyncApplier = edgeCatalogSyncApplier;
    }

    public GetEdgeCatalogDeltaUseCase(
            StoreRepository storeRepository,
            PasswordEncoder passwordEncoder,
            EdgeCatalogSyncApplier edgeCatalogSyncApplier) {
        this(storeRepository, new EdgeDeviceTokenValidator(passwordEncoder), edgeCatalogSyncApplier);
    }

    public EdgeCatalogDeltaResponse execute(String deviceToken, UUID storeId, Instant since) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório para sincronização de cardápio.");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        tokenValidator.validate(deviceToken, store);

        return edgeCatalogSyncApplier.buildDeltaForStore(storeId, since);
    }
}

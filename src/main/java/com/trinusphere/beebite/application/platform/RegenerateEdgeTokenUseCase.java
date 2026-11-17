package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.TenantAccountResponse;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RegenerateEdgeTokenUseCase {

    private final StoreRepository storeRepository;

    @Inject
    public RegenerateEdgeTokenUseCase(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public TenantAccountResponse.StoreSummaryDto execute(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        String newToken = OnboardClientTenantUseCase.generateEdgeDeviceToken();
        store.configureEdgeSync("EDGE", newToken);

        Store saved = storeRepository.save(store);
        return UpdatePlatformStoreUseCase.toStoreSummaryDto(saved);
    }
}


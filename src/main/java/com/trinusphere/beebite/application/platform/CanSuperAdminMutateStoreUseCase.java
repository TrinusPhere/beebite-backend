package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CanSuperAdminMutateStoreUseCase {

    public static final UUID TRINUSPHERE_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final StoreRepository storeRepository;

    @Inject
    public CanSuperAdminMutateStoreUseCase(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public boolean execute(UUID storeId) {
        if (storeId == null || storeRepository == null) {
            return false;
        }
        return storeRepository.findById(storeId)
                .map(store -> TRINUSPHERE_TENANT_ID.equals(store.getTenantId()))
                .orElse(false);
    }
}


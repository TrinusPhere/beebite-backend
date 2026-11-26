package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.InternalStoreCatalogContextResponse;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetInternalStoreCatalogContextUseCase {

    private final StoreRepository storeRepository;

    @Inject
    public GetInternalStoreCatalogContextUseCase(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public InternalStoreCatalogContextResponse execute(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        return new InternalStoreCatalogContextResponse(
                store.getId().toString(),
                store.getTenantId() != null ? store.getTenantId().toString() : null,
                store.getCatalogId() != null ? store.getCatalogId().toString() : store.getId().toString(),
                store.getName(),
                store.getSyncMode(),
                store.isActive()
        );
    }
}


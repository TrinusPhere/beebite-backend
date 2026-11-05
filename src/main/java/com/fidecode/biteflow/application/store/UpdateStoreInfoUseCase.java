package com.fidecode.biteflow.application.store;

import com.fidecode.biteflow.application.store.dto.StoreInfoRequest;
import com.fidecode.biteflow.application.store.dto.StoreInfoResponse;
import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateStoreInfoUseCase {

    private final StoreRepository storeRepository;

    @Inject
    public UpdateStoreInfoUseCase(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public StoreInfoResponse execute(UUID storeId, StoreInfoRequest request) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreNotFoundException(storeId));

        store.updateInfo(request.name(), request.logoUrl(), request.isOpen(), request.openingHours(),
                request.address());
        if (request.publicSlug() != null && !request.publicSlug().isBlank()) {
            store.definePublicSlug(request.publicSlug());
        }

        Store saved = storeRepository.save(store);
        return GetStoreInfoUseCase.toResponse(saved);
    }
}

package com.trinusphere.beebite.application.store;

import com.trinusphere.beebite.application.store.dto.StoreInfoRequest;
import com.trinusphere.beebite.application.store.dto.StoreInfoResponse;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
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

package com.fidecode.biteflow.application.store;

import com.fidecode.biteflow.application.store.dto.StoreInfoResponse;
import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetStoreInfoUseCase {

    private final StoreRepository storeRepository;

    @Inject
    public GetStoreInfoUseCase(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public StoreInfoResponse execute(UUID storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreNotFoundException(storeId));
        return toResponse(store);
    }

    static StoreInfoResponse toResponse(Store s) {
        return new StoreInfoResponse(
            s.getId() != null ? s.getId().toString() : null,
            s.getName(),
            s.getLogoUrl(),
            s.isOpen(),
            s.getOpeningHours(),
            s.getAddress(),
            s.getPublicSlug()
        );
    }
}

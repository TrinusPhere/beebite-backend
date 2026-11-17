package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.TenantAccountResponse;
import com.trinusphere.beebite.application.platform.dto.UpdatePlatformStoreRequest;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdatePlatformStoreUseCase {

    private final StoreRepository storeRepository;

    @Inject
    public UpdatePlatformStoreUseCase(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public TenantAccountResponse.StoreSummaryDto execute(UUID storeId, UpdatePlatformStoreRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        store.updateInfo(
                request.name().trim(),
                store.getLogoUrl(),
                store.isOpen(),
                store.getOpeningHours(),
                request.address()
        );

        String slug = (request.publicSlug() != null && !request.publicSlug().isBlank())
                ? request.publicSlug().trim().toLowerCase()
                : OnboardClientTenantUseCase.slugify(request.name());
        store.definePublicSlug(slug);
        store.assignToTenant(store.getTenantId(), request.cnpj());

        String targetMode = request.syncMode() != null ? request.syncMode().trim().toUpperCase() : "CLOUD";
        if ("EDGE".equals(targetMode)) {
            String token = (store.getEdgeApiKeyHash() != null && !store.getEdgeApiKeyHash().isBlank())
                    ? store.getEdgeApiKeyHash()
                    : OnboardClientTenantUseCase.generateEdgeDeviceToken();
            store.configureEdgeSync("EDGE", token);
        } else {
            store.configureEdgeSync("CLOUD", store.getEdgeApiKeyHash());
        }

        if (request.active()) {
            store.activateStore();
        } else {
            store.deactivateStore();
        }

        Store saved = storeRepository.save(store);
        return toStoreSummaryDto(saved);
    }

    static TenantAccountResponse.StoreSummaryDto toStoreSummaryDto(Store s) {
        return new TenantAccountResponse.StoreSummaryDto(
                s.getId().toString(),
                s.getName(),
                s.getCnpj(),
                s.getAddress(),
                s.getPublicSlug(),
                s.getSyncMode(),
                s.isActive(),
                s.getEdgeApiKeyHash(),
                s.getLastEdgeSyncAt()
        );
    }
}


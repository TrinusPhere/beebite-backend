package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.StoreEntity;

public final class StoreMapper {

    private StoreMapper() {
    }

    public static Store toDomain(StoreEntity e) {
        if (e == null)
            return null;
        return Store.reconstitute(
                e.id,
                e.name,
                e.logoUrl,
                e.isOpen,
                e.openingHours,
                e.address,
                e.publicSlug,
                e.syncMode,
                e.edgeApiKeyHash,
                e.lastEdgeSyncAt,
                e.tenantId,
                e.cnpj,
                e.active
        );
    }

    public static StoreEntity toEntity(Store s) {
        StoreEntity e = new StoreEntity();
        e.id = s.getId();
        e.tenantId = s.getTenantId();
        e.name = s.getName();
        e.cnpj = s.getCnpj();
        e.logoUrl = s.getLogoUrl();
        e.isOpen = s.isOpen();
        e.active = s.isActive();
        e.openingHours = s.getOpeningHours();
        e.address = s.getAddress();
        e.publicSlug = s.getPublicSlug();
        e.syncMode = s.getSyncMode() != null ? s.getSyncMode() : "CLOUD";
        e.edgeApiKeyHash = s.getEdgeApiKeyHash();
        e.lastEdgeSyncAt = s.getLastEdgeSyncAt();
        return e;
    }
}

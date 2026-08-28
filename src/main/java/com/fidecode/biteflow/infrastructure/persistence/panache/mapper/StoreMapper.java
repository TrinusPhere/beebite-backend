package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.StoreEntity;

public final class StoreMapper {

    private StoreMapper() {
    }

    public static Store toDomain(StoreEntity e) {
        if (e == null)
            return null;
        return Store.reconstitute(e.id, e.name, e.logoUrl, e.isOpen, e.openingHours, e.address, e.publicSlug);
    }

    public static StoreEntity toEntity(Store s) {
        StoreEntity e = new StoreEntity();
        e.id = s.getId();
        e.name = s.getName();
        e.logoUrl = s.getLogoUrl();
        e.isOpen = s.isOpen();
        e.openingHours = s.getOpeningHours();
        e.address = s.getAddress();
        e.publicSlug = s.getPublicSlug();
        return e;
    }
}

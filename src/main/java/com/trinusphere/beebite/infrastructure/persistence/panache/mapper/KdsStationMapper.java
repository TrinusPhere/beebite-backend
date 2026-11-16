package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.kds.entity.KdsStation;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.KdsStationEntity;

import java.util.ArrayList;

public final class KdsStationMapper {

    private KdsStationMapper() {}

    public static KdsStation toDomain(KdsStationEntity entity) {
        if (entity == null) return null;
        return KdsStation.reconstitute(
                entity.id,
                entity.storeId,
                entity.name,
                entity.isExpedition,
                entity.active,
                entity.sectorIds != null ? new ArrayList<>(entity.sectorIds) : new ArrayList<>(),
                entity.createdAt,
                entity.updatedAt,
                entity.deletedAt
        );
    }

    public static KdsStationEntity toEntity(KdsStation domain) {
        if (domain == null) return null;
        KdsStationEntity entity = new KdsStationEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.name = domain.getName();
        entity.isExpedition = domain.isExpedition();
        entity.active = domain.isActive();
        entity.sectorIds = domain.getSectorIds() != null ? new ArrayList<>(domain.getSectorIds()) : new ArrayList<>();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }
}

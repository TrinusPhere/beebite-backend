package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.cash.entity.CashRegister;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.CashRegisterEntity;

public final class CashRegisterMapper {

    private CashRegisterMapper() {}

    public static CashRegister toDomain(CashRegisterEntity entity) {
        if (entity == null) return null;
        return CashRegister.reconstitute(
                entity.id,
                entity.storeId,
                entity.name,
                entity.active,
                entity.createdAt,
                entity.updatedAt,
                entity.deletedAt
        );
    }

    public static CashRegisterEntity toEntity(CashRegister domain) {
        if (domain == null) return null;
        CashRegisterEntity entity = new CashRegisterEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.name = domain.getName();
        entity.active = domain.isActive();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }
}

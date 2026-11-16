package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.valueobject.MovementType;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CashMovementEntity;

public final class CashMovementMapper {

    private CashMovementMapper() {}

    public static CashMovement toDomain(CashMovementEntity entity) {
        if (entity == null) return null;
        MovementType type = entity.type != null ? MovementType.valueOf(entity.type) : MovementType.SUPRIMENTO;
        return CashMovement.reconstitute(
                entity.id,
                entity.storeId,
                entity.cashSessionId,
                entity.userId,
                type,
                entity.amount,
                entity.reason,
                entity.createdAt
        );
    }

    public static CashMovementEntity toEntity(CashMovement domain) {
        if (domain == null) return null;
        CashMovementEntity entity = new CashMovementEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.cashSessionId = domain.getCashSessionId();
        entity.userId = domain.getUserId();
        entity.type = domain.getType() != null ? domain.getType().name() : MovementType.SUPRIMENTO.name();
        entity.amount = domain.getAmount();
        entity.reason = domain.getReason();
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }
}

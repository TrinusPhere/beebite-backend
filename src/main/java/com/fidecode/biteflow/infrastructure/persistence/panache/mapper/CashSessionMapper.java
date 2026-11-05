package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.CashSessionEntity;

public final class CashSessionMapper {

    private CashSessionMapper() {}

    public static CashSession toDomain(CashSessionEntity entity) {
        if (entity == null) return null;
        CashSessionStatus status = entity.status != null
                ? CashSessionStatus.valueOf(entity.status)
                : CashSessionStatus.OPEN;

        return CashSession.reconstitute(
                entity.id,
                entity.storeId,
                entity.cashRegisterId,
                entity.userId,
                status,
                entity.openedAt,
                entity.closedAt,
                entity.initialAmount,
                entity.expectedAmount,
                entity.countedAmount,
                entity.differenceAmount,
                entity.closingNotes,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public static CashSessionEntity toEntity(CashSession domain) {
        if (domain == null) return null;
        CashSessionEntity entity = new CashSessionEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.cashRegisterId = domain.getCashRegisterId();
        entity.userId = domain.getUserId();
        entity.status = domain.getStatus() != null ? domain.getStatus().name() : CashSessionStatus.OPEN.name();
        entity.openedAt = domain.getOpenedAt();
        entity.closedAt = domain.getClosedAt();
        entity.initialAmount = domain.getInitialAmount();
        entity.expectedAmount = domain.getExpectedAmount();
        entity.countedAmount = domain.getCountedAmount();
        entity.differenceAmount = domain.getDifferenceAmount();
        entity.closingNotes = domain.getClosingNotes();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }
}

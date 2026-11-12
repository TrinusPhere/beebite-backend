package com.fidecode.beebite.application.cash.dto;

import com.fidecode.beebite.domain.cash.entity.CashMovement;

public final class CashMovementDtoMapper {

    private CashMovementDtoMapper() {}

    public static CashMovementResponse toResponse(CashMovement m) {
        if (m == null) return null;
        return new CashMovementResponse(
                m.getId() != null ? m.getId().toString() : null,
                m.getCashSessionId() != null ? m.getCashSessionId().toString() : null,
                m.getUserId() != null ? m.getUserId().toString() : null,
                m.getType().name(),
                m.getAmount(),
                m.getReason(),
                m.getCreatedAt()
        );
    }
}

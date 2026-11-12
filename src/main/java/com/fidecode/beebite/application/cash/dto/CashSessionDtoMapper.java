package com.fidecode.beebite.application.cash.dto;

import com.fidecode.beebite.domain.cash.entity.CashSession;

public final class CashSessionDtoMapper {

    private CashSessionDtoMapper() {}

    public static CashSessionResponse toResponse(CashSession s) {
        if (s == null) return null;
        return new CashSessionResponse(
                s.getId() != null ? s.getId().toString() : null,
                s.getCashRegisterId() != null ? s.getCashRegisterId().toString() : null,
                s.getUserId() != null ? s.getUserId().toString() : null,
                s.getStatus().name(),
                s.getOpenedAt(),
                s.getClosedAt(),
                s.getInitialAmount(),
                s.getExpectedAmount(),
                s.getCountedAmount(),
                s.getDifferenceAmount(),
                s.getClosingNotes(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}

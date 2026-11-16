package com.trinusphere.beebite.application.cash.dto;

import com.trinusphere.beebite.domain.cash.entity.CashRegister;

public final class CashRegisterDtoMapper {

    private CashRegisterDtoMapper() {}

    public static CashRegisterResponse toResponse(CashRegister r) {
        if (r == null) return null;
        return new CashRegisterResponse(
                r.getId() != null ? r.getId().toString() : null,
                r.getName(),
                r.isActive(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

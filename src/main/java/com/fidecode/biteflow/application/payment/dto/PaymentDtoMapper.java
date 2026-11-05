package com.fidecode.biteflow.application.payment.dto;

import com.fidecode.biteflow.domain.payment.entity.Payment;

public final class PaymentDtoMapper {

    private PaymentDtoMapper() {}

    public static PaymentResponse toResponse(Payment p) {
        if (p == null) return null;
        return new PaymentResponse(
                p.getId() != null ? p.getId().toString() : null,
                p.getOrderId() != null ? p.getOrderId().toString() : null,
                p.getCashSessionId() != null ? p.getCashSessionId().toString() : null,
                p.getDriverId() != null ? p.getDriverId().toString() : null,
                p.getMethod() != null ? p.getMethod().name() : null,
                p.getAmount(),
                p.getChangeFor(),
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getCollectedBy() != null ? p.getCollectedBy().name() : null,
                p.getNotes(),
                p.getPaidAt(),
                p.getCreatedAt()
        );
    }
}

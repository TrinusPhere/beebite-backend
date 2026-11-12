package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import com.fidecode.beebite.domain.payment.entity.Payment;
import com.fidecode.beebite.domain.payment.valueobject.PaymentCollector;
import com.fidecode.beebite.domain.payment.valueobject.PaymentStatus;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.PaymentEntity;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static Payment toDomain(PaymentEntity entity) {
        if (entity == null) return null;
        PaymentMethod method = entity.method != null ? PaymentMethod.valueOf(entity.method) : PaymentMethod.DINHEIRO;
        PaymentStatus status = entity.status != null ? PaymentStatus.valueOf(entity.status) : PaymentStatus.CONFIRMED;
        PaymentCollector collector = entity.collectedBy != null ? PaymentCollector.valueOf(entity.collectedBy) : PaymentCollector.CAIXA;

        return Payment.reconstitute(
                entity.id,
                entity.storeId,
                entity.orderId,
                entity.cashSessionId,
                entity.driverId,
                method,
                entity.amount,
                entity.changeFor,
                status,
                collector,
                entity.notes,
                entity.paidAt,
                entity.createdAt
        );
    }

    public static PaymentEntity toEntity(Payment domain) {
        if (domain == null) return null;
        PaymentEntity entity = new PaymentEntity();
        entity.id = domain.getId();
        entity.storeId = domain.getStoreId();
        entity.orderId = domain.getOrderId();
        entity.cashSessionId = domain.getCashSessionId();
        entity.driverId = domain.getDriverId();
        entity.method = domain.getMethod() != null ? domain.getMethod().name() : PaymentMethod.DINHEIRO.name();
        entity.amount = domain.getAmount();
        entity.changeFor = domain.getChangeFor();
        entity.status = domain.getStatus() != null ? domain.getStatus().name() : PaymentStatus.CONFIRMED.name();
        entity.collectedBy = domain.getCollectedBy() != null ? domain.getCollectedBy().name() : PaymentCollector.CAIXA.name();
        entity.notes = domain.getNotes();
        entity.paidAt = domain.getPaidAt();
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }
}

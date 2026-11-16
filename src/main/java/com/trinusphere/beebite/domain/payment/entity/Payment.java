package com.trinusphere.beebite.domain.payment.entity;

import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.payment.valueobject.PaymentCollector;
import com.trinusphere.beebite.domain.payment.valueobject.PaymentStatus;
import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private UUID orderId;
    private UUID cashSessionId;
    private UUID driverId;
    private PaymentMethod method;
    private BigDecimal amount;
    private BigDecimal changeFor;
    private PaymentStatus status;
    private PaymentCollector collectedBy;
    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public Payment(UUID storeId, UUID orderId, UUID cashSessionId, UUID driverId,
                   PaymentMethod method, BigDecimal amount, BigDecimal changeFor,
                   PaymentCollector collectedBy, String notes) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (method == null) throw new IllegalArgumentException("method não pode ser nulo");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount deve ser maior que zero");
        }
        this.storeId = storeId;
        this.orderId = orderId;
        this.cashSessionId = cashSessionId;
        this.driverId = driverId;
        this.method = method;
        this.amount = amount;
        this.changeFor = changeFor;
        this.collectedBy = collectedBy != null ? collectedBy : PaymentCollector.CAIXA;
        this.notes = notes;
        this.status = PaymentStatus.CONFIRMED;
        this.paidAt = LocalDateTime.now();
        this.createdAt = this.paidAt;
    }

    public static Payment createAtRegister(UUID storeId, UUID orderId, UUID cashSessionId,
                                           PaymentMethod method, BigDecimal amount,
                                           BigDecimal changeFor, String notes) {
        return new Payment(storeId, orderId, cashSessionId, null, method, amount, changeFor, PaymentCollector.CAIXA, notes);
    }

    public static Payment createByDriver(UUID storeId, UUID orderId, UUID cashSessionId, UUID driverId,
                                         PaymentMethod method, BigDecimal amount,
                                         BigDecimal changeFor, String notes) {
        return new Payment(storeId, orderId, cashSessionId, driverId, method, amount, changeFor, PaymentCollector.ENTREGADOR, notes);
    }

    public static Payment reconstitute(UUID id, UUID storeId, UUID orderId, UUID cashSessionId, UUID driverId,
                                       PaymentMethod method, BigDecimal amount, BigDecimal changeFor,
                                       PaymentStatus status, PaymentCollector collectedBy, String notes,
                                       LocalDateTime paidAt, LocalDateTime createdAt) {
        Payment payment = new Payment(storeId, orderId, cashSessionId, driverId, method, amount, changeFor, collectedBy, notes);
        payment.id = id;
        payment.status = status;
        payment.paidAt = paidAt;
        payment.createdAt = createdAt;
        return payment;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    public void linkSession(UUID cashSessionId) {
        this.cashSessionId = cashSessionId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCashSessionId() {
        return cashSessionId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getChangeFor() {
        return changeFor;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentCollector getCollectedBy() {
        return collectedBy;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

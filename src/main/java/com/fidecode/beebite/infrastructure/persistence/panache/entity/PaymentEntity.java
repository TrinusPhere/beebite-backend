package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "order_id")
    public UUID orderId;

    @Column(name = "cash_session_id")
    public UUID cashSessionId;

    @Column(name = "driver_id")
    public UUID driverId;

    @Column(nullable = false, length = 30)
    public String method;

    @Column(nullable = false)
    public BigDecimal amount;

    @Column(name = "change_for")
    public BigDecimal changeFor;

    @Column(nullable = false, length = 20)
    public String status;

    @Column(name = "collected_by", nullable = false, length = 30)
    public String collectedBy;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @Column(name = "paid_at", nullable = false)
    public LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}

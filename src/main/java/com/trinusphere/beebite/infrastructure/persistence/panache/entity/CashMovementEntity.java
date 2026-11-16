package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_movement")
public class CashMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "cash_session_id", nullable = false)
    public UUID cashSessionId;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, length = 30)
    public String type;

    @Column(nullable = false)
    public BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String reason;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}

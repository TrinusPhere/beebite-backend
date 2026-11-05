package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_session")
public class CashSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "cash_register_id", nullable = false)
    public UUID cashRegisterId;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, length = 20)
    public String status;

    @Column(name = "opened_at", nullable = false)
    public LocalDateTime openedAt;

    @Column(name = "closed_at")
    public LocalDateTime closedAt;

    @Column(name = "initial_amount", nullable = false)
    public BigDecimal initialAmount;

    @Column(name = "expected_amount")
    public BigDecimal expectedAmount;

    @Column(name = "counted_amount")
    public BigDecimal countedAmount;

    @Column(name = "difference_amount")
    public BigDecimal differenceAmount;

    @Column(name = "closing_notes", columnDefinition = "TEXT")
    public String closingNotes;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
}

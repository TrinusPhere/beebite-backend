package com.fidecode.biteflow.domain.cash.entity;

import com.fidecode.biteflow.domain.cash.exception.SessionAlreadyClosedException;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CashSession implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private final UUID cashRegisterId;
    private final UUID userId;
    private CashSessionStatus status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private final BigDecimal initialAmount;
    private BigDecimal expectedAmount;
    private BigDecimal countedAmount;
    private BigDecimal differenceAmount;
    private String closingNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CashSession(UUID storeId, UUID cashRegisterId, UUID userId, BigDecimal initialAmount) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (cashRegisterId == null) throw new IllegalArgumentException("cashRegisterId não pode ser nulo");
        if (userId == null) throw new IllegalArgumentException("userId não pode ser nulo");

        this.storeId = storeId;
        this.cashRegisterId = cashRegisterId;
        this.userId = userId;
        this.initialAmount = initialAmount != null ? initialAmount : BigDecimal.ZERO;
        this.status = CashSessionStatus.OPEN;
        this.openedAt = LocalDateTime.now();
        this.createdAt = this.openedAt;
        this.updatedAt = this.openedAt;
    }

    public static CashSession reconstitute(UUID id, UUID storeId, UUID cashRegisterId, UUID userId,
                                          CashSessionStatus status, LocalDateTime openedAt, LocalDateTime closedAt,
                                          BigDecimal initialAmount, BigDecimal expectedAmount,
                                          BigDecimal countedAmount, BigDecimal differenceAmount,
                                          String closingNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        CashSession session = new CashSession(storeId, cashRegisterId, userId, initialAmount);
        session.id = id;
        session.status = status;
        if (openedAt != null) session.openedAt = openedAt;
        session.closedAt = closedAt;
        session.expectedAmount = expectedAmount;
        session.countedAmount = countedAmount;
        session.differenceAmount = differenceAmount;
        session.closingNotes = closingNotes;
        session.createdAt = createdAt;
        session.updatedAt = updatedAt;
        return session;
    }

    public void close(BigDecimal expectedAmount, BigDecimal countedAmount, String closingNotes) {
        if (this.status == CashSessionStatus.CLOSED) {
            throw new SessionAlreadyClosedException("Esta sessão de caixa já se encontra fechada.");
        }
        this.status = CashSessionStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.expectedAmount = expectedAmount != null ? expectedAmount : BigDecimal.ZERO;
        this.countedAmount = countedAmount != null ? countedAmount : BigDecimal.ZERO;
        this.differenceAmount = this.countedAmount.subtract(this.expectedAmount);
        this.closingNotes = closingNotes;
        this.updatedAt = this.closedAt;
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

    public UUID getCashRegisterId() {
        return cashRegisterId;
    }

    public UUID getUserId() {
        return userId;
    }

    public CashSessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public BigDecimal getInitialAmount() {
        return initialAmount;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public BigDecimal getCountedAmount() {
        return countedAmount;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public String getClosingNotes() {
        return closingNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

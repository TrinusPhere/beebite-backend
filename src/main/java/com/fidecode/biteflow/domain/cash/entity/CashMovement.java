package com.fidecode.biteflow.domain.cash.entity;

import com.fidecode.biteflow.domain.cash.exception.InvalidMovementException;
import com.fidecode.biteflow.domain.cash.valueobject.MovementType;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CashMovement implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private final UUID cashSessionId;
    private final UUID userId;
    private final MovementType type;
    private final BigDecimal amount;
    private final String reason;
    private final LocalDateTime createdAt;

    public CashMovement(UUID storeId, UUID cashSessionId, UUID userId,
                        MovementType type, BigDecimal amount, String reason) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (cashSessionId == null) throw new IllegalArgumentException("cashSessionId não pode ser nulo");
        if (userId == null) throw new IllegalArgumentException("userId não pode ser nulo");
        if (type == null) throw new IllegalArgumentException("type não pode ser nulo");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidMovementException("O valor da movimentação deve ser maior que zero.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidMovementException("A justificativa da movimentação é obrigatória.");
        }

        this.storeId = storeId;
        this.cashSessionId = cashSessionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.reason = reason.trim();
        this.createdAt = LocalDateTime.now();
    }

    public static CashMovement reconstitute(UUID id, UUID storeId, UUID cashSessionId, UUID userId,
                                            MovementType type, BigDecimal amount, String reason,
                                            LocalDateTime createdAt) {
        CashMovement movement = new CashMovement(storeId, cashSessionId, userId, type, amount, reason);
        movement.id = id;
        return movement;
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

    public UUID getCashSessionId() {
        return cashSessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

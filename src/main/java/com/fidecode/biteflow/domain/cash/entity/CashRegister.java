package com.fidecode.biteflow.domain.cash.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.time.LocalDateTime;
import java.util.UUID;

public class CashRegister implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public CashRegister(UUID storeId, String name) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Nome do caixa não pode ser vazio");

        this.storeId = storeId;
        this.name = name.trim();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static CashRegister reconstitute(UUID id, UUID storeId, String name, boolean active,
                                           LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        CashRegister register = new CashRegister(storeId, name);
        register.id = id;
        register.active = active;
        register.createdAt = createdAt;
        register.updatedAt = updatedAt;
        register.deletedAt = deletedAt;
        return register;
    }

    public void rename(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Nome do caixa não pode ser vazio");
        this.name = name.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = this.deletedAt;
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

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}

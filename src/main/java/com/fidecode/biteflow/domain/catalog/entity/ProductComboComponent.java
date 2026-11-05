package com.fidecode.biteflow.domain.catalog.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductComboComponent implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private final UUID comboProductId;
    private final UUID componentProductId;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public ProductComboComponent(UUID storeId, UUID comboProductId, UUID componentProductId, int quantity) {
        if (storeId == null) throw new IllegalArgumentException("storeId não pode ser nulo");
        if (comboProductId == null) throw new IllegalArgumentException("comboProductId não pode ser nulo");
        if (componentProductId == null) throw new IllegalArgumentException("componentProductId não pode ser nulo");
        if (quantity <= 0) throw new IllegalArgumentException("quantity deve ser maior que zero");

        this.storeId = storeId;
        this.comboProductId = comboProductId;
        this.componentProductId = componentProductId;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
    }

    public static ProductComboComponent reconstitute(UUID id, UUID storeId, UUID comboProductId,
                                                     UUID componentProductId, int quantity,
                                                     LocalDateTime createdAt, LocalDateTime deletedAt) {
        ProductComboComponent comp = new ProductComboComponent(storeId, comboProductId, componentProductId, quantity);
        comp.id = id;
        comp.createdAt = createdAt;
        comp.deletedAt = deletedAt;
        return comp;
    }

    public void updateQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity deve ser maior que zero");
        this.quantity = quantity;
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
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

    public UUID getComboProductId() {
        return comboProductId;
    }

    public UUID getComponentProductId() {
        return componentProductId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}


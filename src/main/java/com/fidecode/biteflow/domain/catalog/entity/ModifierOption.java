package com.fidecode.biteflow.domain.catalog.entity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain entity — ModifierOption (opção dentro de um grupo de modificadores).
 */
public class ModifierOption {

    private UUID id;
    private UUID modifierGroupId;
    private String name;
    private BigDecimal price;
    private int sortOrder;
    private UUID linkedProductId;

    public ModifierOption(UUID modifierGroupId, String name, BigDecimal price, int sortOrder) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.modifierGroupId = modifierGroupId;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.sortOrder = sortOrder;
    }

    // ── Getters ──
    public UUID getId() {
        return id;
    }

    public UUID getModifierGroupId() {
        return modifierGroupId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public UUID getLinkedProductId() {
        return linkedProductId;
    }

    // ── Setters ──
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setLinkedProductId(UUID linkedProductId) {
        this.linkedProductId = linkedProductId;
    }
}

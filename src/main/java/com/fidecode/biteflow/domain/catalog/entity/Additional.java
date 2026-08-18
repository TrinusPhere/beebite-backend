package com.fidecode.biteflow.domain.catalog.entity;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain entity — Additional (adicional global).
 */
public class Additional implements StoreGuard.StoreScoped {

    private UUID id;
    private UUID storeId;
    private String name;
    private BigDecimal price;
    private String category;
    private boolean active;

    public Additional(UUID storeId, String name, BigDecimal price, String category) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.category = category;
        this.active = true;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    // ── Getters ──
    public UUID getId()             { return id; }
    public UUID getStoreId()        { return storeId; }
    public String getName()         { return name; }
    public BigDecimal getPrice()    { return price; }
    public String getCategory()     { return category; }
    public boolean isActive()       { return active; }

    // ── Setters ──
    public void setId(UUID id)                  { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setPrice(BigDecimal price)      { this.price = price; }
    public void setCategory(String category)    { this.category = category; }
    public void setActive(boolean active)       { this.active = active; }
}

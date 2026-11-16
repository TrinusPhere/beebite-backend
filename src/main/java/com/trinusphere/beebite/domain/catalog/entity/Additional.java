package com.trinusphere.beebite.domain.catalog.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;
import java.math.BigDecimal;
import java.util.UUID;

public class Additional implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private BigDecimal price;
    private String category;
    private boolean active;

    public Additional(UUID storeId, String name, BigDecimal price, String category) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.category = category;
        active = true;
    }

    public static Additional reconstitute(UUID id, UUID storeId, String name, BigDecimal price, String category,
            boolean active) {
        Additional a = new Additional(storeId, name, price, category);
        a.id = id;
        a.active = active;
        return a;
    }

    public void update(String name, BigDecimal price, String category) {
        if (name != null && !name.isBlank())
            this.name = name;
        if (price != null)
            this.price = price;
        this.category = category;
    }

    public void toggleActive() {
        active = !active;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
package com.trinusphere.beebite.domain.catalog.entity;

import com.trinusphere.beebite.domain.shared.CatalogGuard;
import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.math.BigDecimal;
import java.util.UUID;

public class Additional implements StoreGuard.StoreScoped, CatalogGuard.CatalogScoped {
    private UUID id;
    private UUID catalogId;
    private final UUID storeId;
    private String name;
    private BigDecimal price;
    private String category;
    private boolean active;

    public Additional(UUID storeId, String name, BigDecimal price, String category) {
        this(storeId, storeId, name, price, category);
    }

    private Additional(UUID catalogId, UUID storeId, String name, BigDecimal price, String category) {
        if (storeId == null && catalogId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.catalogId = catalogId != null ? catalogId : storeId;
        this.storeId = storeId != null ? storeId : catalogId;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.category = category;
        this.active = true;
    }

    public static Additional reconstitute(UUID id, UUID storeId, String name, BigDecimal price, String category,
            boolean active) {
        return reconstituteForCatalog(id, storeId, storeId, name, price, category, active);
    }

    public static Additional reconstituteForCatalog(UUID id, UUID catalogId, UUID contextStoreId, String name,
            BigDecimal price, String category, boolean active) {
        Additional a = new Additional(catalogId, contextStoreId, name, price, category);
        a.id = id;
        a.active = active;
        return a;
    }

    public void assignCatalog(UUID catalogId) {
        if (catalogId != null) {
            this.catalogId = catalogId;
        }
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

    @Override
    public UUID getCatalogId() {
        return catalogId;
    }

    @Override
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

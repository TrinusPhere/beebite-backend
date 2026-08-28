package com.fidecode.biteflow.domain.catalog.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.time.LocalDateTime;
import java.util.*;

public class ProductCategory implements StoreGuard.StoreScoped {
    private final UUID id;
    private final UUID storeId;
    private String name;
    private boolean active;
    private int displayOrder;
    private List<UUID> defaultModifierGroupIds;
    private final LocalDateTime createdAt;

    private ProductCategory(UUID id, UUID storeId, String name, boolean active, int displayOrder, List<UUID> ids,
            LocalDateTime createdAt) {
        if (storeId == null)
            throw new IllegalArgumentException("StoreId is required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.active = active;
        this.displayOrder = displayOrder;
        this.defaultModifierGroupIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public static ProductCategory create(UUID storeId, String name, int order, List<UUID> ids) {
        return new ProductCategory(UUID.randomUUID(), storeId, name, true, order, ids, LocalDateTime.now());
    }

    public static ProductCategory reconstitute(UUID id, UUID storeId, String name, boolean active, int order,
            List<UUID> ids, LocalDateTime createdAt) {
        return new ProductCategory(id, storeId, name, active, order, ids, createdAt);
    }

    public void update(String name, boolean active, int order, List<UUID> ids) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
        this.name = name;
        this.active = active;
        this.displayOrder = order;
        this.defaultModifierGroupIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
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

    public boolean isActive() {
        return active;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public List<UUID> getDefaultModifierGroupIds() {
        return Collections.unmodifiableList(defaultModifierGroupIds);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
package com.fidecode.biteflow.domain.catalog.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductCategory {
    private UUID id;
    private UUID storeId;
    private String name;
    private boolean isActive;
    private int displayOrder;
    private List<UUID> defaultModifierGroupIds;
    private LocalDateTime createdAt;

    public ProductCategory(UUID id, UUID storeId, String name, boolean isActive, int displayOrder, List<UUID> defaultModifierGroupIds, LocalDateTime createdAt) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
        this.defaultModifierGroupIds = defaultModifierGroupIds != null ? defaultModifierGroupIds : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public static ProductCategory create(UUID storeId, String name, int displayOrder, List<UUID> defaultModifierGroupIds) {
        if (storeId == null) throw new IllegalArgumentException("StoreId is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");

        return new ProductCategory(
                UUID.randomUUID(),
                storeId,
                name,
                true,
                displayOrder,
                defaultModifierGroupIds,
                LocalDateTime.now()
        );
    }

    public void update(String name, boolean isActive, int displayOrder, List<UUID> defaultModifierGroupIds) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        this.name = name;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
        this.defaultModifierGroupIds = defaultModifierGroupIds != null ? defaultModifierGroupIds : new ArrayList<>();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public String getName() { return name; }
    public boolean isActive() { return isActive; }
    public int getDisplayOrder() { return displayOrder; }
    public List<UUID> getDefaultModifierGroupIds() { return defaultModifierGroupIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

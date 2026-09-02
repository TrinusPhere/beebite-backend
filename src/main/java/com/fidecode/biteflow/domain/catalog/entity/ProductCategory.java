package com.fidecode.biteflow.domain.catalog.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import com.fidecode.biteflow.domain.catalog.valueobject.ProductStatus;
import java.time.LocalDateTime;
import java.util.*;

public class ProductCategory implements StoreGuard.StoreScoped {
    private final UUID id;
    private final UUID storeId;
    private String name;
    private ProductStatus status;
    private int displayOrder;
    private List<UUID> defaultModifierGroupIds;
    private final LocalDateTime createdAt;

    private ProductCategory(UUID id, UUID storeId, String name, ProductStatus status, int displayOrder, List<UUID> ids,
            LocalDateTime createdAt) {
        if (storeId == null)
            throw new IllegalArgumentException("StoreId is required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.status = status != null ? status : ProductStatus.AVAILABLE;
        this.displayOrder = displayOrder;
        this.defaultModifierGroupIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public static ProductCategory create(UUID storeId, String name, int order, List<UUID> ids) {
        return new ProductCategory(UUID.randomUUID(), storeId, name, ProductStatus.AVAILABLE, order, ids, LocalDateTime.now());
    }

    public static ProductCategory reconstitute(UUID id, UUID storeId, String name, ProductStatus status, int order,
            List<UUID> ids, LocalDateTime createdAt) {
        return new ProductCategory(id, storeId, name, status, order, ids, createdAt);
    }

    public void update(String name, ProductStatus status, int order, List<UUID> ids) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
        this.name = name;
        this.status = status != null ? status : ProductStatus.AVAILABLE;
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

    public ProductStatus getStatus() {
        return status;
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
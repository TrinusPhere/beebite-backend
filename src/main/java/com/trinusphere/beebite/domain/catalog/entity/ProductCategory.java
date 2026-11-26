package com.trinusphere.beebite.domain.catalog.entity;

import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.shared.CatalogGuard;
import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.time.LocalDateTime;
import java.util.*;

public class ProductCategory implements StoreGuard.StoreScoped, CatalogGuard.CatalogScoped {
    private final UUID id;
    private UUID catalogId;
    private final UUID storeId;
    private String name;
    private ProductStatus status;
    private int displayOrder;
    private List<UUID> defaultModifierGroupIds;
    private final LocalDateTime createdAt;

    private ProductCategory(UUID id, UUID catalogId, UUID storeId, String name, ProductStatus status, int displayOrder,
            List<UUID> ids, LocalDateTime createdAt) {
        if (storeId == null && catalogId == null)
            throw new IllegalArgumentException("StoreId is required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
        this.id = id;
        this.catalogId = catalogId != null ? catalogId : storeId;
        this.storeId = storeId != null ? storeId : catalogId;
        this.name = name;
        this.status = status != null ? status : ProductStatus.AVAILABLE;
        this.displayOrder = displayOrder;
        this.defaultModifierGroupIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public static ProductCategory create(UUID storeId, String name, int order, List<UUID> ids) {
        return new ProductCategory(UUID.randomUUID(), storeId, storeId, name, ProductStatus.AVAILABLE, order, ids,
                LocalDateTime.now());
    }

    public static ProductCategory reconstitute(UUID id, UUID storeId, String name, ProductStatus status, int order,
            List<UUID> ids, LocalDateTime createdAt) {
        return new ProductCategory(id, storeId, storeId, name, status, order, ids, createdAt);
    }

    public static ProductCategory reconstituteForCatalog(UUID id, UUID catalogId, UUID contextStoreId, String name,
            ProductStatus status, int order, List<UUID> ids, LocalDateTime createdAt) {
        return new ProductCategory(id, catalogId, contextStoreId, name, status, order, ids, createdAt);
    }

    public void assignCatalog(UUID catalogId) {
        if (catalogId != null) {
            this.catalogId = catalogId;
        }
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

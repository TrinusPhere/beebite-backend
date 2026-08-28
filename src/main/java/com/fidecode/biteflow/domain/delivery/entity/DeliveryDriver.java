package com.fidecode.biteflow.domain.delivery.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryDriver implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private String phone;
    private DriverStatus status;
    private boolean fixed;
    private boolean daily;
    private BigDecimal allowance;
    private boolean hasFixedDeliveryValue;
    private BigDecimal fixedDeliveryValue;
    private int totalDeliveries;
    private BigDecimal rating;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeliveryDriver(UUID storeId, String name, String phone) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.phone = phone;
        this.status = DriverStatus.ATIVO;
        allowance = BigDecimal.ZERO;
        fixedDeliveryValue = BigDecimal.ZERO;
        rating = BigDecimal.ZERO;
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    public static DeliveryDriver reconstitute(UUID id, UUID storeId, String name, String phone, DriverStatus status,
            boolean fixed, boolean daily, BigDecimal allowance, boolean hasFixedDeliveryValue,
            BigDecimal fixedDeliveryValue, int totalDeliveries, BigDecimal rating, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        DeliveryDriver d = new DeliveryDriver(storeId, name, phone);
        d.id = id;
        d.status = status;
        d.fixed = fixed;
        d.daily = daily;
        d.allowance = allowance;
        d.hasFixedDeliveryValue = hasFixedDeliveryValue;
        d.fixedDeliveryValue = fixedDeliveryValue;
        d.totalDeliveries = totalDeliveries;
        d.rating = rating;
        d.updatedAt = updatedAt;
        return new DeliveryDriver(id, storeId, name, phone, status, fixed, daily, allowance, hasFixedDeliveryValue,
                fixedDeliveryValue, totalDeliveries, rating, createdAt, updatedAt);
    }

    private DeliveryDriver(UUID id, UUID storeId, String name, String phone, DriverStatus status, boolean fixed,
            boolean daily, BigDecimal allowance, boolean hasFixedDeliveryValue, BigDecimal fixedDeliveryValue,
            int totalDeliveries, BigDecimal rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.fixed = fixed;
        this.daily = daily;
        this.allowance = allowance;
        this.hasFixedDeliveryValue = hasFixedDeliveryValue;
        this.fixedDeliveryValue = fixedDeliveryValue;
        this.totalDeliveries = totalDeliveries;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateProfile(String name, String phone) {
        if (name != null && !name.isBlank())
            this.name = name;
        if (phone != null)
            this.phone = phone;
        touch();
    }

    public void configureWork(DriverStatus status, Boolean fixed, Boolean daily, BigDecimal allowance,
            Boolean hasFixedDeliveryValue, BigDecimal fixedDeliveryValue) {
        if (status != null)
            this.status = status;
        if (fixed != null)
            this.fixed = fixed;
        if (daily != null)
            this.daily = daily;
        if (allowance != null)
            this.allowance = allowance;
        if (hasFixedDeliveryValue != null)
            this.hasFixedDeliveryValue = hasFixedDeliveryValue;
        if (fixedDeliveryValue != null)
            this.fixedDeliveryValue = fixedDeliveryValue;
        touch();
    }

    public void incrementDeliveries() {
        totalDeliveries++;
        touch();
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
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

    public String getPhone() {
        return phone;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isDaily() {
        return daily;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public boolean isHasFixedDeliveryValue() {
        return hasFixedDeliveryValue;
    }

    public BigDecimal getFixedDeliveryValue() {
        return fixedDeliveryValue;
    }

    public int getTotalDeliveries() {
        return totalDeliveries;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
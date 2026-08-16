package com.fidecode.biteflow.domain.delivery.entity;

import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity — POJO puro, zero dependência de framework.
 */
public class DeliveryDriver {

    private UUID id;
    private UUID storeId;
    private String name;
    private String phone;
    private DriverStatus status;
    private boolean isFixed;
    private boolean isDaily;
    private BigDecimal allowance;
    private boolean hasFixedDeliveryValue;
    private BigDecimal fixedDeliveryValue;
    private int totalDeliveries;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeliveryDriver() {}

    public DeliveryDriver(UUID storeId, String name, String phone) {
        this.storeId = storeId;
        this.name = name;
        this.phone = phone;
        this.status = DriverStatus.ATIVO;
        this.isFixed = false;
        this.isDaily = false;
        this.allowance = BigDecimal.ZERO;
        this.hasFixedDeliveryValue = false;
        this.fixedDeliveryValue = BigDecimal.ZERO;
        this.totalDeliveries = 0;
        this.rating = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId()                       { return id; }
    public UUID getStoreId()                  { return storeId; }
    public String getName()                   { return name; }
    public String getPhone()                  { return phone; }
    public DriverStatus getStatus()           { return status; }
    public boolean isFixed()                  { return isFixed; }
    public boolean isDaily()                  { return isDaily; }
    public BigDecimal getAllowance()           { return allowance; }
    public boolean isHasFixedDeliveryValue()  { return hasFixedDeliveryValue; }
    public BigDecimal getFixedDeliveryValue() { return fixedDeliveryValue; }
    public int getTotalDeliveries()           { return totalDeliveries; }
    public BigDecimal getRating()             { return rating; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }

    // Setters
    public void setId(UUID id)                                   { this.id = id; }
    public void setStoreId(UUID storeId)                         { this.storeId = storeId; }
    public void setName(String name)                             { this.name = name; }
    public void setPhone(String phone)                           { this.phone = phone; }
    public void setStatus(DriverStatus status)                   { this.status = status; }
    public void setFixed(boolean fixed)                          { isFixed = fixed; }
    public void setDaily(boolean daily)                          { isDaily = daily; }
    public void setAllowance(BigDecimal allowance)               { this.allowance = allowance; }
    public void setHasFixedDeliveryValue(boolean v)              { this.hasFixedDeliveryValue = v; }
    public void setFixedDeliveryValue(BigDecimal v)              { this.fixedDeliveryValue = v; }
    public void setTotalDeliveries(int totalDeliveries)          { this.totalDeliveries = totalDeliveries; }
    public void setRating(BigDecimal rating)                     { this.rating = rating; }
    public void setCreatedAt(LocalDateTime createdAt)            { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)            { this.updatedAt = updatedAt; }

    public void incrementDeliveries() {
        this.totalDeliveries++;
    }
}

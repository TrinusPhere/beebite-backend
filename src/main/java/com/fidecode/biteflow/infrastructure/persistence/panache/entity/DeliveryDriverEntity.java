package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_driver")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class DeliveryDriverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(nullable = false)
    private String status;

    @Column(name = "is_fixed", nullable = false)
    private boolean isFixed;

    @Column(name = "is_daily", nullable = false)
    private boolean isDaily;

    @Column(nullable = false)
    private BigDecimal allowance;

    @Column(name = "has_fixed_delivery_value", nullable = false)
    private boolean hasFixedDeliveryValue;

    @Column(name = "fixed_delivery_value", nullable = false)
    private BigDecimal fixedDeliveryValue;

    @Column(name = "total_deliveries", nullable = false)
    private int totalDeliveries;

    @Column(nullable = false)
    private BigDecimal rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean daily) {
        isDaily = daily;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public void setAllowance(BigDecimal a) {
        this.allowance = a;
    }

    public boolean isHasFixedDeliveryValue() {
        return hasFixedDeliveryValue;
    }

    public void setHasFixedDeliveryValue(boolean v) {
        this.hasFixedDeliveryValue = v;
    }

    public BigDecimal getFixedDeliveryValue() {
        return fixedDeliveryValue;
    }

    public void setFixedDeliveryValue(BigDecimal v) {
        this.fixedDeliveryValue = v;
    }

    public int getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(int t) {
        this.totalDeliveries = t;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal r) {
        this.rating = r;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime c) {
        this.createdAt = c;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime u) {
        this.updatedAt = u;
    }

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingredient")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class IngredientEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id", nullable = false)
    public UUID storeId;
    @Column(nullable = false)
    public String name;
    @Column(precision = 12, scale = 3)
    public BigDecimal stock;
    @Column(length = 10)
    public String unit;
    @Column(name = "cost_price", precision = 10, scale = 2)
    public BigDecimal costPrice;
    @Column(name = "min_stock", precision = 12, scale = 3)
    public BigDecimal minStock;
    @Column(nullable = false)
    public boolean active;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

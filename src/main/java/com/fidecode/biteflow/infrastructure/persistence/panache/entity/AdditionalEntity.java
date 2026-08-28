package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import java.time.LocalDateTime;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "additional")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class AdditionalEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(nullable = false)
    public String name;

    @Column(precision = 10, scale = 2)
    public BigDecimal price;

    public String category;

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

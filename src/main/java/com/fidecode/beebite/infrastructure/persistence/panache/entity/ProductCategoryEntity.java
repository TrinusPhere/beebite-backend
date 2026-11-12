package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import java.time.LocalDateTime;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_category")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class ProductCategoryEntity extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(nullable = false)
    public String name;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "display_order", nullable = false)
    public int displayOrder;

    @Column(name = "default_modifier_group_ids", columnDefinition = "TEXT")
    public String defaultModifierGroupIds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

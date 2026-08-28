package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "production_sector")
@SQLRestriction("deleted_at IS NULL")
public class ProductionSectorEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(name = "store_id", nullable = false)
    public UUID storeId;
    public String code;
    @Column(nullable = false)
    public String name;
    public boolean active;
    @CreationTimestamp
    @Column(name = "created_at")
    public Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

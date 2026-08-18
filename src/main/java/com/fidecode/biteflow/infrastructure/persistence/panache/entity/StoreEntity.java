package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import java.time.LocalDateTime;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "store")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class StoreEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "logo_url")
    public String logoUrl;

    @Column(name = "is_open", nullable = false)
    public boolean isOpen;

    @Column(name = "opening_hours")
    public String openingHours;

    @Column(name = "address")
    public String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @jakarta.persistence.Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

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

    @Column(name = "opening_hours", columnDefinition = "TEXT")
    public String openingHours;

    @Column(name = "address")
    public String address;

    @Column(name = "public_slug")
    public String publicSlug;

    @Column(name = "sync_mode", nullable = false)
    public String syncMode = "CLOUD";

    @Column(name = "edge_api_key_hash")
    public String edgeApiKeyHash;

    @Column(name = "last_edge_sync_at")
    public Instant lastEdgeSyncAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}

package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "edge_sync_outbox")
public class EdgeOutboxEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "event_type", nullable = false, length = 60)
    public String eventType;

    @Column(name = "aggregate_id", nullable = false)
    public UUID aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    public String payload;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "attempts", nullable = false)
    public int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    public String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "synced_at")
    public Instant syncedAt;
}

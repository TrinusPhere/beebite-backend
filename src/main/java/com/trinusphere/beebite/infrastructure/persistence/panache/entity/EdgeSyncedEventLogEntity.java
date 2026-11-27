package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "edge_synced_event_log")
public class EdgeSyncedEventLogEntity extends PanacheEntityBase {

    @Id
    @Column(name = "event_id", updatable = false, nullable = false)
    public UUID eventId;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "sequence")
    public Long sequence;

    @Column(name = "event_type", nullable = false, length = 60)
    public String eventType;

    @Column(name = "aggregate_id", nullable = false)
    public UUID aggregateId;

    @Column(name = "outcome", nullable = false, length = 20)
    public String outcome;

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    public String rejectedReason;

    @Column(name = "received_at", nullable = false)
    public Instant receivedAt;
}

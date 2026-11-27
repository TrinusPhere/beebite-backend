package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "edge_sync_stream_state")
public class EdgeSyncStreamStateEntity extends PanacheEntityBase {

    @Id
    @Column(name = "store_id", updatable = false, nullable = false)
    public UUID storeId;

    @Column(name = "cloud_ack_sequence", nullable = false)
    public long cloudAckSequence;

    @Column(name = "consecutive_failures", nullable = false)
    public int consecutiveFailures;

    @Column(name = "next_attempt_at")
    public Instant nextAttemptAt;

    @Column(name = "last_success_at")
    public Instant lastSuccessAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    public String lastError;

    @Column(name = "full_resync_requested", nullable = false)
    public boolean fullResyncRequested;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}


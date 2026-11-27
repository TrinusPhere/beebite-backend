package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "edge_sync_cursor")
public class EdgeSyncCursorEntity extends PanacheEntityBase {

    @Id
    @Column(name = "store_id", updatable = false, nullable = false)
    public UUID storeId;

    @Column(name = "last_contiguous_sequence", nullable = false)
    public long lastContiguousSequence;

    @Column(name = "initialized_at", nullable = false, updatable = false)
    public Instant initializedAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}


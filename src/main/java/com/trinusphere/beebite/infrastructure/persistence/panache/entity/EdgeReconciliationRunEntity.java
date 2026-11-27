package com.trinusphere.beebite.infrastructure.persistence.panache.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "edge_reconciliation_run")
public class EdgeReconciliationRunEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(name = "trigger", nullable = false, length = 20)
    public String trigger;

    @Column(name = "window_from", nullable = false)
    public LocalDate windowFrom;

    @Column(name = "window_to", nullable = false)
    public LocalDate windowTo;

    @Column(name = "status", nullable = false, length = 20)
    public String status;

    @Column(name = "buckets_compared", nullable = false)
    public int bucketsCompared;

    @Column(name = "buckets_mismatched", nullable = false)
    public int bucketsMismatched;

    @Column(name = "requeued_count", nullable = false)
    public int requeuedCount;

    @Column(name = "extra_in_cloud_count", nullable = false)
    public int extraInCloudCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "started_at", nullable = false, updatable = false)
    public Instant startedAt;

    @Column(name = "finished_at")
    public Instant finishedAt;
}


package com.fidecode.biteflow.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kds_station")
public class KdsStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(name = "store_id", nullable = false)
    public UUID storeId;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(name = "is_expedition", nullable = false)
    public boolean isExpedition;

    @Column(nullable = false)
    public boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "kds_station_sector", joinColumns = @JoinColumn(name = "station_id"))
    @Column(name = "sector_id")
    public List<UUID> sectorIds = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}


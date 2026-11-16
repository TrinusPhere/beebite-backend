package com.trinusphere.beebite.domain.kds.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class KdsStation implements StoreGuard.StoreScoped {

    private UUID id;
    private final UUID storeId;
    private String name;
    private boolean isExpedition;
    private boolean active;
    private final List<UUID> sectorIds = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public KdsStation(UUID storeId, String name, boolean isExpedition, List<UUID> sectorIds) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId não pode ser nulo");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da estação KDS não pode ser vazio");
        }
        this.storeId = storeId;
        this.name = name.trim();
        this.isExpedition = isExpedition;
        this.active = true;
        if (sectorIds != null) {
            this.sectorIds.addAll(sectorIds);
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static KdsStation reconstitute(UUID id, UUID storeId, String name, boolean isExpedition,
                                         boolean active, List<UUID> sectorIds,
                                         LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        KdsStation station = new KdsStation(storeId, name, isExpedition, sectorIds);
        station.id = id;
        station.active = active;
        station.createdAt = createdAt;
        station.updatedAt = updatedAt;
        station.deletedAt = deletedAt;
        return station;
    }

    public void update(String name, boolean isExpedition, List<UUID> sectorIds) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da estação KDS não pode ser vazio");
        }
        this.name = name.trim();
        this.isExpedition = isExpedition;
        this.sectorIds.clear();
        if (sectorIds != null) {
            this.sectorIds.addAll(sectorIds);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = this.deletedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public boolean isExpedition() {
        return isExpedition;
    }

    public boolean isActive() {
        return active;
    }

    public List<UUID> getSectorIds() {
        return Collections.unmodifiableList(sectorIds);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}


package com.trinusphere.beebite.domain.catalog.entity;

import java.time.Instant;
import java.util.UUID;

public class Catalog {
    private UUID id;
    private final UUID tenantId;
    private String name;
    private boolean defaultCatalog;
    private final Instant createdAt;
    private Instant updatedAt;

    public Catalog(UUID tenantId, String name, boolean defaultCatalog) {
        if (tenantId == null) {
            throw new IllegalArgumentException("O tenantId do cardápio é obrigatório.");
        }
        validateName(name);
        this.tenantId = tenantId;
        this.name = name.trim();
        this.defaultCatalog = defaultCatalog;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private Catalog(UUID id, UUID tenantId, String name, boolean defaultCatalog, Instant createdAt, Instant updatedAt) {
        if (tenantId == null) {
            throw new IllegalArgumentException("O tenantId do cardápio é obrigatório.");
        }
        validateName(name);
        this.id = id;
        this.tenantId = tenantId;
        this.name = name.trim();
        this.defaultCatalog = defaultCatalog;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Catalog reconstitute(
            UUID id,
            UUID tenantId,
            String name,
            boolean defaultCatalog,
            Instant createdAt,
            Instant updatedAt) {
        return new Catalog(id, tenantId, name, defaultCatalog, createdAt, updatedAt);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do cardápio é obrigatório.");
        }
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
        this.updatedAt = Instant.now();
    }

    public void markAsDefault() {
        this.defaultCatalog = true;
        this.updatedAt = Instant.now();
    }

    public void unmarkDefault() {
        this.defaultCatalog = false;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultCatalog() {
        return defaultCatalog;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}


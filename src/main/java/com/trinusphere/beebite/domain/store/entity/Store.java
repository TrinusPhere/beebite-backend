package com.trinusphere.beebite.domain.store.entity;

import java.time.Instant;
import java.util.UUID;

public class Store {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String cnpj;
    private String logoUrl;
    private boolean open;
    private boolean active;
    private String openingHours;
    private String address;
    private String publicSlug;
    private String syncMode;
    private String edgeApiKeyHash;
    private Instant lastEdgeSyncAt;

    public Store(String name) {
        validate(name);
        this.name = name;
        this.syncMode = "CLOUD";
        this.active = true;
    }

    public static Store reconstitute(UUID id, String name, String logoUrl, boolean open, String hours, String address,
            String slug) {
        return reconstitute(id, name, logoUrl, open, hours, address, slug, "CLOUD", null, null, null, null, true);
    }

    public static Store reconstitute(UUID id, String name, String logoUrl, boolean open, String hours, String address,
            String slug, String syncMode, String edgeApiKeyHash, Instant lastEdgeSyncAt) {
        return reconstitute(id, name, logoUrl, open, hours, address, slug, syncMode, edgeApiKeyHash, lastEdgeSyncAt,
                null, null, true);
    }

    public static Store reconstitute(UUID id, String name, String logoUrl, boolean open, String hours, String address,
            String slug, String syncMode, String edgeApiKeyHash, Instant lastEdgeSyncAt, UUID tenantId, String cnpj,
            boolean active) {
        Store s = new Store(name);
        s.id = id;
        s.logoUrl = logoUrl;
        s.open = open;
        s.openingHours = hours;
        s.address = address;
        s.publicSlug = slug;
        s.syncMode = (syncMode == null || syncMode.isBlank()) ? "CLOUD" : syncMode;
        s.edgeApiKeyHash = edgeApiKeyHash;
        s.lastEdgeSyncAt = lastEdgeSyncAt;
        s.tenantId = tenantId;
        s.cnpj = cnpj;
        s.active = active;
        return s;
    }

    private static void validate(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("O nome da loja é obrigatório.");
    }

    public void updateInfo(String name, String logoUrl, boolean open, String hours, String address) {
        validate(name);
        this.name = name;
        this.logoUrl = logoUrl;
        this.open = open;
        this.openingHours = hours;
        this.address = address;
    }

    public void definePublicSlug(String slug) {
        this.publicSlug = slug;
    }

    public void assignToTenant(UUID tenantId, String cnpj) {
        this.tenantId = tenantId;
        this.cnpj = cnpj;
    }

    public void activateStore() {
        this.active = true;
    }

    public void deactivateStore() {
        this.active = false;
        this.open = false;
    }

    public void configureEdgeSync(String syncMode, String edgeApiKeyHash) {
        if (syncMode == null || (!syncMode.equals("CLOUD") && !syncMode.equals("EDGE"))) {
            throw new IllegalArgumentException("syncMode deve ser CLOUD ou EDGE.");
        }
        this.syncMode = syncMode;
        this.edgeApiKeyHash = edgeApiKeyHash;
    }

    public void markEdgeSynced(Instant syncedAt) {
        this.lastEdgeSyncAt = syncedAt != null ? syncedAt : Instant.now();
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isActive() {
        return active;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public String getAddress() {
        return address;
    }

    public String getPublicSlug() {
        return publicSlug;
    }

    public String getSyncMode() {
        return syncMode;
    }

    public String getEdgeApiKeyHash() {
        return edgeApiKeyHash;
    }

    public Instant getLastEdgeSyncAt() {
        return lastEdgeSyncAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

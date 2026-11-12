package com.fidecode.beebite.domain.store.entity;

import java.time.Instant;
import java.util.UUID;

public class Store {
    private UUID id;
    private String name;
    private String logoUrl;
    private boolean open;
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
    }

    public static Store reconstitute(UUID id, String name, String logoUrl, boolean open, String hours, String address,
            String slug) {
        return reconstitute(id, name, logoUrl, open, hours, address, slug, "CLOUD", null, null);
    }

    public static Store reconstitute(UUID id, String name, String logoUrl, boolean open, String hours, String address,
            String slug, String syncMode, String edgeApiKeyHash, Instant lastEdgeSyncAt) {
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

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isOpen() {
        return open;
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

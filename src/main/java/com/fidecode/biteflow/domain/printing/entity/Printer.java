package com.fidecode.biteflow.domain.printing.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.util.UUID;

public class Printer implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private String connectionType;
    private String address;
    private boolean active;

    public Printer(UUID storeId, String name, String connectionType) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.connectionType = connectionType != null ? connectionType : "NETWORK";
        this.active = true;
    }

    public static Printer reconstitute(UUID id, UUID storeId, String name, String connectionType, String address,
            boolean active) {
        var p = new Printer(storeId, name, connectionType);
        p.id = id;
        p.address = address;
        p.active = active;
        return p;
    }

    public void configureAddress(String address) {
        this.address = address;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    public void rename(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

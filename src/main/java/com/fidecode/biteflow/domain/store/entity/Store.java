package com.fidecode.biteflow.domain.store.entity;

import java.util.UUID;

/**
 * Domain entity — Store (loja).
 * POJO puro, sem dependência de framework.
 */
public class Store {

    private UUID id;
    private String name;
    private String logoUrl;
    private boolean open;
    private String openingHours;
    private String address;

    public Store(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da loja é obrigatório.");
        }
        this.name = name;
        this.open = false;
    }

    // ── Getters ──────────────────────────────────────

    public UUID getId()           { return id; }
    public String getName()       { return name; }
    public String getLogoUrl()    { return logoUrl; }
    public boolean isOpen()       { return open; }
    public String getOpeningHours(){ return openingHours; }
    public String getAddress()    { return address; }

    // ── Setters ──────────────────────────────────────

    public void setId(UUID id)                   { this.id = id; }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da loja é obrigatório.");
        }
        this.name = name;
    }
    public void setLogoUrl(String logoUrl)       { this.logoUrl = logoUrl; }
    public void setOpen(boolean open)            { this.open = open; }
    public void setOpeningHours(String hours)    { this.openingHours = hours; }
    public void setAddress(String address)       { this.address = address; }
}

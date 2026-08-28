package com.fidecode.biteflow.domain.production.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.util.UUID;

public class ProductionSector implements StoreGuard.StoreScoped {
    private UUID id; private final UUID storeId; private String code; private String name; private boolean active;
    public ProductionSector(UUID storeId, String name) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        this.storeId=storeId; this.name=name; this.active=true;
    }
    public static ProductionSector reconstitute(UUID id, UUID storeId, String code, String name, boolean active) {
        var sector = new ProductionSector(storeId, name); sector.id=id; sector.code=code; sector.active=active; return sector;
    }
    public void configure(String code, Boolean active) { this.code = code; if (active != null) this.active = active; }
    public void rename(String name) { if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório."); this.name=name; }
    public void activate(){this.active=true;} public void deactivate(){this.active=false;}
    public UUID getId(){return id;} public UUID getStoreId(){return storeId;} public String getCode(){return code;} public String getName(){return name;} public boolean isActive(){return active;}
    public void setId(UUID id){this.id=id;}
}

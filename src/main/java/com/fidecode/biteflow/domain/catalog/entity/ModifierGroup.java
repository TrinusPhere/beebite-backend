package com.fidecode.biteflow.domain.catalog.entity;
import com.fidecode.biteflow.domain.shared.StoreGuard;

import com.fidecode.biteflow.domain.catalog.valueobject.ModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity — ModifierGroup.
 */
public class ModifierGroup implements StoreGuard.StoreScoped {

    private UUID id;
    private UUID storeId;
    private String name;
    private ModifierType type;
    private boolean required;
    private int minSelections;
    private int maxSelections;
    private String icon;
    private List<ModifierOption> options;

    public ModifierGroup(UUID storeId, String name, ModifierType type, boolean required,
                         int minSelections, int maxSelections, String icon) {
        if (storeId == null) throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório.");
        if (minSelections > maxSelections) throw new IllegalArgumentException("minSelections não pode ser maior que maxSelections.");
        this.storeId = storeId;
        this.name = name;
        this.type = type != null ? type : ModifierType.RADIO;
        this.required = required;
        this.minSelections = minSelections;
        this.maxSelections = maxSelections;
        this.icon = icon;
        this.options = new ArrayList<>();
    }

    // ── Getters ──
    public UUID getId()                     { return id; }
    public UUID getStoreId()                { return storeId; }
    public String getName()                 { return name; }
    public ModifierType getType()           { return type; }
    public boolean isRequired()             { return required; }
    public int getMinSelections()           { return minSelections; }
    public int getMaxSelections()           { return maxSelections; }
    public String getIcon()                 { return icon; }
    public List<ModifierOption> getOptions(){ return options; }

    // ── Setters ──
    public void setId(UUID id)                          { this.id = id; }
    public void setName(String name)                    { this.name = name; }
    public void setType(ModifierType type)              { this.type = type; }
    public void setRequired(boolean required)           { this.required = required; }
    public void setMinSelections(int min)               { this.minSelections = min; }
    public void setMaxSelections(int max)               { this.maxSelections = max; }
    public void setIcon(String icon)                    { this.icon = icon; }
    public void setOptions(List<ModifierOption> opts)   { this.options = opts != null ? opts : new ArrayList<>(); }
}

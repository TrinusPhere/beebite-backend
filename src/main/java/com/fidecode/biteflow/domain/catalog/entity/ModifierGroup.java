package com.fidecode.biteflow.domain.catalog.entity;

import com.fidecode.biteflow.domain.shared.StoreGuard;
import com.fidecode.biteflow.domain.catalog.valueobject.ModifierType;
import com.fidecode.biteflow.domain.catalog.valueobject.ModifierGroupSemanticType;
import java.util.*;

public class ModifierGroup implements StoreGuard.StoreScoped {
    private UUID id;
    private final UUID storeId;
    private String name;
    private ModifierType type;
    private boolean required;
    private int minSelections;
    private int maxSelections;
    private String icon;
    private ModifierGroupSemanticType semanticType;
    private List<ModifierOption> options;

    public ModifierGroup(UUID storeId, String name, ModifierType type, boolean required, int min, int max,
            String icon, ModifierGroupSemanticType semanticType) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        validate(name, min, max);
        this.storeId = storeId;
        this.name = name;
        this.type = type != null ? type : ModifierType.RADIO;
        this.required = required;
        this.minSelections = min;
        this.maxSelections = max;
        this.icon = icon;
        this.semanticType = semanticType;
        this.options = new ArrayList<>();
    }

    public static ModifierGroup reconstitute(UUID id, UUID storeId, String name, ModifierType type, boolean required,
            int min, int max, String icon, ModifierGroupSemanticType semanticType, List<ModifierOption> options) {
        ModifierGroup g = new ModifierGroup(storeId, name, type, required, min, max, icon, semanticType);
        g.id = id;
        g.replaceOptions(options);
        return g;
    }

    private static void validate(String name, int min, int max) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        if (min > max)
            throw new IllegalArgumentException("minSelections não pode ser maior que maxSelections.");
    }

    public void update(String name, ModifierType type, Boolean required, Integer min, Integer max, String icon, ModifierGroupSemanticType semanticType) {
        int nextMin = min != null ? min : minSelections, nextMax = max != null ? max : maxSelections;
        validate(name != null ? name : this.name, nextMin, nextMax);
        if (name != null && !name.isBlank())
            this.name = name;
        if (type != null)
            this.type = type;
        if (required != null)
            this.required = required;
        this.minSelections = nextMin;
        this.maxSelections = nextMax;
        this.icon = icon;
        if (semanticType != null) {
            this.semanticType = semanticType;
        }
    }

    public void replaceOptions(List<ModifierOption> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
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

    public ModifierType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public int getMinSelections() {
        return minSelections;
    }

    public int getMaxSelections() {
        return maxSelections;
    }

    public String getIcon() {
        return icon;
    }

    public ModifierGroupSemanticType getSemanticType() {
        return semanticType;
    }

    public List<ModifierOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
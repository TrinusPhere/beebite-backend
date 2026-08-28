package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.entity.ModifierOption;
import com.fidecode.biteflow.domain.catalog.valueobject.ModifierType;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierGroupEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierOptionEntity;

import java.util.ArrayList;
import java.util.List;

public final class ModifierGroupMapper {
    private ModifierGroupMapper() {}

    public static ModifierGroup toDomain(ModifierGroupEntity e) {
        if (e == null) return null;
        List<ModifierOption> opts = e.options != null ? e.options.stream().map(o -> {
            ModifierOption opt = new ModifierOption(e.id, o.name, o.price, o.sortOrder); opt.setId(o.id); return opt;
        }).toList() : List.of();
        return ModifierGroup.reconstitute(e.id, e.storeId, e.name, ModifierType.valueOf(e.type), e.required, e.minSelections, e.maxSelections, e.icon, opts);
    }

    public static ModifierGroupEntity toEntity(ModifierGroup g) {
        ModifierGroupEntity e = new ModifierGroupEntity();
        e.id = g.getId();
        e.storeId = g.getStoreId();
        e.name = g.getName();
        e.type = g.getType().name();
        e.required = g.isRequired();
        e.minSelections = g.getMinSelections();
        e.maxSelections = g.getMaxSelections();
        e.icon = g.getIcon();
        
        if (g.getOptions() != null) {
            for (ModifierOption opt : g.getOptions()) {
                ModifierOptionEntity oe = new ModifierOptionEntity();
                oe.id = opt.getId();
                oe.modifierGroup = e;
                oe.name = opt.getName();
                oe.price = opt.getPrice();
                oe.sortOrder = opt.getSortOrder();
                e.options.add(oe);
            }
        }
        return e;
    }
}

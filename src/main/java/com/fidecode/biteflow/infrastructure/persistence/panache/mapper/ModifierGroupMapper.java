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
        ModifierGroup g = new ModifierGroup(e.storeId, e.name,
            ModifierType.valueOf(e.type), e.required, e.minSelections, e.maxSelections, e.icon);
        g.setId(e.id);
        if (e.options != null) {
            List<ModifierOption> opts = e.options.stream().map(o -> {
                ModifierOption opt = new ModifierOption(o.modifierGroupId, o.name, o.price, o.sortOrder);
                opt.setId(o.id);
                return opt;
            }).toList();
            g.setOptions(opts);
        }
        return g;
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
        // We don't map options here because PanacheModifierGroupRepository handles them manually
        return e;
    }
}

package com.trinusphere.beebite.infrastructure.persistence.panache.mapper;

import com.trinusphere.beebite.domain.printing.entity.PrintRule;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.PrintRuleEntity;

public final class PrintRuleMapper {
    private PrintRuleMapper() {
    }

    public static PrintRule toDomain(PrintRuleEntity e) {
        return PrintRule.reconstitute(e.id, e.storeId, e.name, e.operation, e.documentType, e.sectorId, e.printerId,
                e.active);
    }

    public static PrintRuleEntity toEntity(PrintRule r) {
        var e = new PrintRuleEntity();
        e.id = r.getId();
        e.storeId = r.getStoreId();
        e.name = r.getName();
        e.operation = r.getOperation();
        e.documentType = r.getDocumentType();
        e.sectorId = r.getSectorId();
        e.printerId = r.getPrinterId();
        e.active = r.isActive();
        return e;
    }
}

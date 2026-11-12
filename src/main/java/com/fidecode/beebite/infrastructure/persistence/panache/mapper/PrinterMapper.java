package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.printing.entity.Printer;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.PrinterEntity;

public final class PrinterMapper {
    private PrinterMapper() {
    }

    public static Printer toDomain(PrinterEntity e) {
        return Printer.reconstitute(e.id, e.storeId, e.name, e.connectionType, e.address, e.active, e.paperWidthMm,
                e.port, e.codePage, e.cutPaper);
    }

    public static PrinterEntity toEntity(Printer p) {
        var e = new PrinterEntity();
        e.id = p.getId();
        e.storeId = p.getStoreId();
        e.name = p.getName();
        e.connectionType = p.getConnectionType();
        e.address = p.getAddress();
        e.active = p.isActive();
        e.paperWidthMm = p.getPaperWidth().millimeters();
        e.port = p.getPort();
        e.codePage = p.getCodePage();
        e.cutPaper = p.isCutPaper();
        return e;
    }
}

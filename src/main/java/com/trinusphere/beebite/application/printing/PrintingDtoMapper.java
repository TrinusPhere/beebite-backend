package com.trinusphere.beebite.application.printing;

import com.trinusphere.beebite.application.printing.dto.*;
import com.trinusphere.beebite.domain.printing.entity.*;
import com.trinusphere.beebite.domain.printing.valueobject.ReceiptSections;

public final class PrintingDtoMapper {
    private PrintingDtoMapper() {
    }

    public static PrinterResponse toResponse(Printer p) {
        return new PrinterResponse(p.getId() != null ? p.getId().toString() : null, p.getName(), p.getConnectionType(),
                p.getAddress(), p.isActive(), p.getPaperWidth().millimeters(), p.getPaperWidth().columns(),
                p.getPort(), p.getCodePage(), p.isCutPaper());
    }

    public static PrintRuleResponse toResponse(PrintRule r) {
        return new PrintRuleResponse(r.getId() != null ? r.getId().toString() : null, r.getName(), r.getOperation(),
                r.getDocumentType(), r.getSectorId() != null ? r.getSectorId().toString() : null,
                r.getPrinterId().toString(), r.isActive());
    }

    public static PrintTemplateResponse toResponse(PrintTemplate t) {
        return new PrintTemplateResponse(t.getDocumentType().name(), toSectionsDto(t.getSections()), t.getHeaderText(),
                t.getFooterText(), t.isLargeTitle());
    }

    public static PrintTemplateRequest.Sections toSectionsDto(ReceiptSections s) {
        return new PrintTemplateRequest.Sections(s.showStoreName(), s.showStoreAddress(), s.showCustomerName(),
                s.showCustomerPhone(), s.showCustomerAddress(), s.showOrderNumber(), s.showDateTime(),
                s.showChannel(), s.showServiceType(), s.showOrderNotes(), s.showItemModifiers(),
                s.showItemRemovals(), s.showItemNotes(), s.showItemPrices(), s.showSubtotal(), s.showDeliveryFee(),
                s.showTotal(), s.showPaymentMethod(), s.showCashChange());
    }

    public static ReceiptSections toSections(PrintTemplateRequest.Sections s) {
        return new ReceiptSections(s.showStoreName(), s.showStoreAddress(), s.showCustomerName(),
                s.showCustomerPhone(), s.showCustomerAddress(), s.showOrderNumber(), s.showDateTime(),
                s.showChannel(), s.showServiceType(), s.showOrderNotes(), s.showItemModifiers(),
                s.showItemRemovals(), s.showItemNotes(), s.showItemPrices(), s.showSubtotal(), s.showDeliveryFee(),
                s.showTotal(), s.showPaymentMethod(), s.showCashChange());
    }
}

package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import com.fidecode.biteflow.domain.printing.valueobject.ReceiptSections;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.PrintTemplateEntity;

public final class PrintTemplateMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PrintTemplateMapper() {
    }

    public static PrintTemplate toDomain(PrintTemplateEntity e) {
        DocumentType docType = DocumentType.fromValue(e.documentType);
        ReceiptSections sections = parseSections(e.sections, docType);
        return PrintTemplate.reconstitute(
                e.id,
                e.storeId,
                docType,
                sections,
                e.headerText,
                e.footerText,
                e.largeTitle
        );
    }

    public static PrintTemplateEntity toEntity(PrintTemplate t) {
        var e = new PrintTemplateEntity();
        e.id = t.getId();
        e.storeId = t.getStoreId();
        e.documentType = t.getDocumentType().name();
        e.sections = serializeSections(t.getSections());
        e.headerText = t.getHeaderText();
        e.footerText = t.getFooterText();
        e.largeTitle = t.isLargeTitle();
        return e;
    }

    private static ReceiptSections parseSections(String json, DocumentType docType) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return switch (docType) {
                case KITCHEN -> ReceiptSections.kitchenDefaults();
                case EXPEDITION -> ReceiptSections.expeditionDefaults();
                case CUSTOMER_RECEIPT -> ReceiptSections.customerReceiptDefaults();
            };
        }
        try {
            return MAPPER.readValue(json, ReceiptSections.class);
        } catch (Exception ex) {
            return ReceiptSections.none();
        }
    }

    private static String serializeSections(ReceiptSections sections) {
        if (sections == null) {
            return "{}";
        }
        try {
            return MAPPER.writeValueAsString(sections);
        } catch (Exception ex) {
            return "{}";
        }
    }
}

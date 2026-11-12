package com.fidecode.beebite.domain.printing.entity;

import com.fidecode.beebite.domain.printing.valueobject.DocumentType;
import com.fidecode.beebite.domain.printing.valueobject.ReceiptSections;
import com.fidecode.beebite.domain.shared.StoreGuard;
import java.util.UUID;

/** Modelo de comanda de um tipo de documento: seções visíveis, cabeçalho, rodapé e destaque do título. */
public class PrintTemplate implements StoreGuard.StoreScoped {
    private static final int MAX_TEXT = 255;

    private UUID id;
    private final UUID storeId;
    private final DocumentType documentType;
    private ReceiptSections sections;
    private String headerText;
    private String footerText;
    private boolean largeTitle;

    private PrintTemplate(UUID storeId, DocumentType documentType, ReceiptSections sections) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (documentType == null)
            throw new IllegalArgumentException("documentType é obrigatório.");
        this.storeId = storeId;
        this.documentType = documentType;
        this.sections = sections;
        this.largeTitle = true;
    }

    public static PrintTemplate defaultFor(UUID storeId, DocumentType documentType) {
        if (documentType == null)
            throw new IllegalArgumentException("documentType é obrigatório.");
        ReceiptSections sections = switch (documentType) {
            case KITCHEN -> ReceiptSections.kitchenDefaults();
            case EXPEDITION -> ReceiptSections.expeditionDefaults();
            case CUSTOMER_RECEIPT -> ReceiptSections.customerReceiptDefaults();
        };
        return new PrintTemplate(storeId, documentType, sections);
    }

    public static PrintTemplate reconstitute(UUID id, UUID storeId, DocumentType documentType,
            ReceiptSections sections, String headerText, String footerText, boolean largeTitle) {
        var t = new PrintTemplate(storeId, documentType, sections);
        t.id = id;
        t.headerText = headerText;
        t.footerText = footerText;
        t.largeTitle = largeTitle;
        return t;
    }

    public void update(ReceiptSections sections, String headerText, String footerText, boolean largeTitle) {
        if (sections == null)
            throw new IllegalArgumentException("sections é obrigatório.");
        this.headerText = normalize(headerText, "Cabeçalho");
        this.footerText = normalize(footerText, "Rodapé");
        this.sections = sections;
        this.largeTitle = largeTitle;
    }

    private static String normalize(String text, String label) {
        if (text == null || text.isBlank())
            return null;
        String trimmed = text.trim();
        if (trimmed.length() > MAX_TEXT)
            throw new IllegalArgumentException(label + " não pode ultrapassar " + MAX_TEXT + " caracteres.");
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public ReceiptSections getSections() {
        return sections;
    }

    public String getHeaderText() {
        return headerText;
    }

    public String getFooterText() {
        return footerText;
    }

    public boolean isLargeTitle() {
        return largeTitle;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}


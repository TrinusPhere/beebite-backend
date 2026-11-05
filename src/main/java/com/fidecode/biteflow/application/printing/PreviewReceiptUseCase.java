package com.fidecode.biteflow.application.printing;

import com.fidecode.biteflow.application.printing.dto.PreviewReceiptRequest;
import com.fidecode.biteflow.application.printing.dto.ReceiptPreviewResponse;
import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.repository.PrintTemplateRepository;
import com.fidecode.biteflow.domain.printing.service.ReceiptLayoutService;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import com.fidecode.biteflow.domain.printing.valueobject.PaperWidth;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Pré-visualização ao vivo de um modelo com um pedido de exemplo. Usa o modelo enviado no corpo (sem salvar) ou, se
 * ausente, o modelo salvo.
 */
@ApplicationScoped
public class PreviewReceiptUseCase {
    private final StoreRepository stores;
    private final PrintTemplateRepository templates;
    private final ReceiptLayoutService layout;

    @Inject
    public PreviewReceiptUseCase(StoreRepository stores, PrintTemplateRepository templates,
            ReceiptLayoutService layout) {
        this.stores = stores;
        this.templates = templates;
        this.layout = layout;
    }

    public ReceiptPreviewResponse execute(UUID storeId, String documentType, PreviewReceiptRequest request) {
        DocumentType type = DocumentType.fromValue(documentType);
        PaperWidth width = PaperWidth.fromMillimeters(request.paperWidthMm());
        var store = stores.findById(storeId).orElseThrow(() -> new StoreNotFoundException(storeId));

        PrintTemplate template;
        if (request.template() != null) {
            template = PrintTemplate.defaultFor(storeId, type);
            template.update(PrintingDtoMapper.toSections(request.template().sections()),
                    request.template().headerText(), request.template().footerText(),
                    request.template().largeTitle() == null || request.template().largeTitle());
        } else {
            template = templates.findByStoreIdAndDocumentType(storeId, type)
                    .orElseGet(() -> PrintTemplate.defaultFor(storeId, type));
        }

        var doc = layout.build(SampleOrderFactory.create(storeId), store, template, width);
        var lines = doc.render().stream()
                .map(l -> new ReceiptPreviewResponse.Line(l.text(), l.bold(), l.large())).toList();
        return new ReceiptPreviewResponse(width.columns(), width.millimeters(), lines);
    }
}


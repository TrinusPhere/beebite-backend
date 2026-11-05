package com.fidecode.biteflow.application.printing;

import com.fidecode.biteflow.application.printing.dto.PrintTemplateRequest;
import com.fidecode.biteflow.application.printing.dto.PrintTemplateResponse;
import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.repository.PrintTemplateRepository;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UpdatePrintTemplateUseCase {
    private final PrintTemplateRepository repo;

    @Inject
    public UpdatePrintTemplateUseCase(PrintTemplateRepository repo) {
        this.repo = repo;
    }

    public PrintTemplateResponse execute(UUID storeId, String documentType, PrintTemplateRequest request) {
        DocumentType type = DocumentType.fromValue(documentType);
        PrintTemplate template = repo.findByStoreIdAndDocumentType(storeId, type)
                .orElseGet(() -> PrintTemplate.defaultFor(storeId, type));
        template.update(PrintingDtoMapper.toSections(request.sections()), request.headerText(), request.footerText(),
                request.largeTitle() == null || request.largeTitle());
        return PrintingDtoMapper.toResponse(repo.save(template));
    }
}


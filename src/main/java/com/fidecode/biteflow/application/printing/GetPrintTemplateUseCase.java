package com.fidecode.biteflow.application.printing;

import com.fidecode.biteflow.application.printing.dto.PrintTemplateResponse;
import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.repository.PrintTemplateRepository;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/** Devolve o modelo salvo do tipo de documento ou, se ainda não configurado, o padrão do tipo. */
@ApplicationScoped
public class GetPrintTemplateUseCase {
    private final PrintTemplateRepository repo;

    @Inject
    public GetPrintTemplateUseCase(PrintTemplateRepository repo) {
        this.repo = repo;
    }

    public PrintTemplateResponse execute(UUID storeId, String documentType) {
        DocumentType type = DocumentType.fromValue(documentType);
        return PrintingDtoMapper.toResponse(repo.findByStoreIdAndDocumentType(storeId, type)
                .orElseGet(() -> PrintTemplate.defaultFor(storeId, type)));
    }
}


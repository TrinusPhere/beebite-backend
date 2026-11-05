package com.fidecode.biteflow.domain.printing.repository;

import com.fidecode.biteflow.domain.printing.entity.PrintTemplate;
import com.fidecode.biteflow.domain.printing.valueobject.DocumentType;
import java.util.Optional;
import java.util.UUID;

public interface PrintTemplateRepository {
    Optional<PrintTemplate> findByStoreIdAndDocumentType(UUID storeId, DocumentType documentType);

    PrintTemplate save(PrintTemplate template);
}


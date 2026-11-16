package com.trinusphere.beebite.domain.printing.repository;

import com.trinusphere.beebite.domain.printing.entity.PrintTemplate;
import com.trinusphere.beebite.domain.printing.valueobject.DocumentType;
import java.util.Optional;
import java.util.UUID;

public interface PrintTemplateRepository {
    Optional<PrintTemplate> findByStoreIdAndDocumentType(UUID storeId, DocumentType documentType);

    PrintTemplate save(PrintTemplate template);
}


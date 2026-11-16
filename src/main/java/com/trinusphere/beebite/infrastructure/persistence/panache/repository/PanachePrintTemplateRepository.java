package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.printing.entity.PrintTemplate;
import com.trinusphere.beebite.domain.printing.repository.PrintTemplateRepository;
import com.trinusphere.beebite.domain.printing.valueobject.DocumentType;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.PrintTemplateMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanachePrintTemplateRepository implements PrintTemplateRepository {

    @Inject
    PrintTemplatePanacheRepo repo;

    @Override
    public Optional<PrintTemplate> findByStoreIdAndDocumentType(UUID storeId, DocumentType documentType) {
        return repo.find("storeId = ?1 and documentType = ?2", storeId, documentType.name())
                .firstResultOptional()
                .map(PrintTemplateMapper::toDomain);
    }

    @Override
    @Transactional
    public PrintTemplate save(PrintTemplate template) {
        var entity = PrintTemplateMapper.toEntity(template);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return PrintTemplateMapper.toDomain(entity);
    }
}

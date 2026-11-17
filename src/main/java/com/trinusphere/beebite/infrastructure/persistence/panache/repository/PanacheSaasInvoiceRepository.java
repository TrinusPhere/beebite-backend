package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.SaasInvoiceEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.SaasInvoiceMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheSaasInvoiceRepository implements SaasInvoiceRepository {

    @Inject
    SaasInvoicePanacheRepo repo;

    @Override
    @Transactional
    public SaasInvoice save(SaasInvoice invoice) {
        SaasInvoiceEntity entity = SaasInvoiceMapper.toEntity(invoice);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return SaasInvoiceMapper.toDomain(entity);
    }

    @Override
    public Optional<SaasInvoice> findById(UUID id) {
        return repo.findByIdOptional(id).map(SaasInvoiceMapper::toDomain);
    }

    @Override
    public Optional<SaasInvoice> findByContractAndMonth(UUID contractId, String referenceMonth) {
        return repo.find("contractId = ?1 and referenceMonth = ?2", contractId, referenceMonth)
                .firstResultOptional()
                .map(SaasInvoiceMapper::toDomain);
    }

    @Override
    public List<SaasInvoice> findByTenantId(UUID tenantId) {
        return repo.find("tenantId = ?1 order by dueDate desc", tenantId)
                .stream()
                .map(SaasInvoiceMapper::toDomain)
                .toList();
    }

    @Override
    public List<SaasInvoice> findAll() {
        return repo.find("order by dueDate desc, createdAt desc")
                .stream()
                .map(SaasInvoiceMapper::toDomain)
                .toList();
    }
}


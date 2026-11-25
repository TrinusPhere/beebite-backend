package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.FinancialRecordEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.FinancialRecordMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do port FinancialRecordRepository via Panache. Segue o padrão dual-class (FinancialRecordPanacheRepo +
 * este).
 */
@ApplicationScoped
public class PanacheFinancialRecordRepository implements FinancialRecordRepository {

    private final FinancialRecordPanacheRepo repo;
    private final com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;
    private final com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer;

    public PanacheFinancialRecordRepository(FinancialRecordPanacheRepo repo) {
        this(repo, null, null);
    }

    @Inject
    public PanacheFinancialRecordRepository(
            FinancialRecordPanacheRepo repo,
            com.trinusphere.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase,
            com.trinusphere.beebite.infrastructure.sync.EdgeSyncPayloadSerializer payloadSerializer) {
        this.repo = repo;
        this.enqueueEdgeOutboxUseCase = enqueueEdgeOutboxUseCase;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    @Transactional
    public FinancialRecord save(FinancialRecord record) {
        FinancialRecordEntity entity = FinancialRecordMapper.toEntity(record);
        if (entity.id == null) {
            repo.getEntityManager().persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        FinancialRecord saved = FinancialRecordMapper.toDomain(entity);
        record.setId(entity.id);
        if (enqueueEdgeOutboxUseCase != null && payloadSerializer != null && saved.getId() != null) {
            enqueueEdgeOutboxUseCase.execute(
                    saved.getStoreId(),
                    "FINANCIAL_RECORD_UPSERT",
                    saved.getId(),
                    payloadSerializer.serializeFinancialRecord(saved)
            );
        }
        return saved;
    }

    @Override
    public Optional<FinancialRecord> findById(UUID id) {
        return repo.findByIdOptional(id).map(FinancialRecordMapper::toDomain);
    }

    @Override
    public List<FinancialRecord> findByStoreIdAndDateRange(UUID storeId, LocalDate startDate, LocalDate endDate) {
        return repo.find("storeId = ?1 and recordDate >= ?2 and recordDate <= ?3", storeId, startDate, endDate).list()
                .stream().map(FinancialRecordMapper::toDomain).toList();
    }
}

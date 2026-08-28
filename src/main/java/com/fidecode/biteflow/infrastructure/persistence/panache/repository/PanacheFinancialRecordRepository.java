package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.FinancialRecordEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.FinancialRecordMapper;

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

    @Inject
    public PanacheFinancialRecordRepository(FinancialRecordPanacheRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public FinancialRecord save(FinancialRecord record) {
        FinancialRecordEntity entity = FinancialRecordMapper.toEntity(record);
        repo.getEntityManager().persist(entity);
        record.setId(entity.id);
        return record;
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

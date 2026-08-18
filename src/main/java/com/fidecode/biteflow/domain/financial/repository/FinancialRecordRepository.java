package com.fidecode.biteflow.domain.financial.repository;

import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port — interface do repositório de registros financeiros.
 */
public interface FinancialRecordRepository {

    FinancialRecord save(FinancialRecord record);

    Optional<FinancialRecord> findById(UUID id);

    List<FinancialRecord> findByStoreIdAndDateRange(UUID storeId, LocalDate startDate, LocalDate endDate);
}

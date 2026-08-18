package com.fidecode.biteflow.application.financial;

import com.fidecode.biteflow.application.financial.dto.FinancialRecordResponse;
import com.fidecode.biteflow.domain.financial.repository.FinancialRecordRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Use Case — Lista registros financeiros por intervalo de datas.
 */
@ApplicationScoped
public class ListFinancialRecordsUseCase {

    private final FinancialRecordRepository financialRecordRepository;

    @Inject
    public ListFinancialRecordsUseCase(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public List<FinancialRecordResponse> execute(UUID storeId, long startEpoch, long endEpoch) {
        LocalDate startDate = epochToLocalDate(startEpoch);
        LocalDate endDate = epochToLocalDate(endEpoch);

        return financialRecordRepository.findByStoreIdAndDateRange(storeId, startDate, endDate)
            .stream()
            .map(FinancialDtoMapper::toResponse)
            .toList();
    }

    private LocalDate epochToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

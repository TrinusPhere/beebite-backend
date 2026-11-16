package com.trinusphere.beebite.application.financial;

import com.trinusphere.beebite.application.financial.dto.SalesSummaryResponse;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Use Case — Resumo de vendas por período. Recebe startDate/endDate como epoch (ms). Calcula variação % vs período
 * anterior.
 */
@ApplicationScoped
public class GetSalesSummaryUseCase {

    private final FinancialRecordRepository financialRecordRepository;

    @Inject
    public GetSalesSummaryUseCase(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public SalesSummaryResponse execute(UUID storeId, long startEpoch, long endEpoch) {
        LocalDate startDate = epochToLocalDate(startEpoch);
        LocalDate endDate = epochToLocalDate(endEpoch);

        // Busca registros do período atual
        List<FinancialRecord> currentRecords = financialRecordRepository.findByStoreIdAndDateRange(storeId, startDate,
                endDate);

        // Filtra apenas CONCLUIDO
        List<FinancialRecord> validRecords = currentRecords.stream()
                .filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).toList();

        BigDecimal revenue = validRecords.stream().map(FinancialRecord::getTotalValue).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        int totalOrders = validRecords.size();

        // Calcula variação vs período anterior de mesmo tamanho
        long daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevEnd = startDate.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(daysInPeriod - 1);

        List<FinancialRecord> previousRecords = financialRecordRepository.findByStoreIdAndDateRange(storeId, prevStart,
                prevEnd);

        BigDecimal previousRevenue = previousRecords.stream().filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO)
                .map(FinancialRecord::getTotalValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        String changePercentage = calculateChangePercentage(revenue, previousRevenue);

        return new SalesSummaryResponse(startEpoch, endEpoch, revenue, changePercentage, totalOrders);
    }

    private String calculateChangePercentage(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return "0%";
            }
            return "+100%";
        }
        BigDecimal change = current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);

        String prefix = change.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + change.stripTrailingZeros().toPlainString() + "%";
    }

    private LocalDate epochToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

package com.trinusphere.beebite.application.financial;

import com.trinusphere.beebite.application.financial.dto.GeneralCmvRequest;
import com.trinusphere.beebite.application.financial.dto.GeneralCmvResponse;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.financial.valueobject.CmvAlertLevel;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Use Case — Calcula CMV geral do período. Fórmula: CMV = estoque_inicial + compras - estoque_final CMV% = (CMV /
 * faturamento_bruto) * 100
 */
@ApplicationScoped
public class CalculateGeneralCmvUseCase {

    private final FinancialRecordRepository financialRecordRepository;

    @Inject
    public CalculateGeneralCmvUseCase(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public GeneralCmvResponse execute(UUID storeId, GeneralCmvRequest request) {
        LocalDate startDate = epochToLocalDate(request.startDate());
        LocalDate endDate = epochToLocalDate(request.endDate());

        // Faturamento bruto do período
        BigDecimal grossRevenue = financialRecordRepository.findByStoreIdAndDateRange(storeId, startDate, endDate)
                .stream().filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).map(FinancialRecord::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CMV = estoque_inicial + compras - estoque_final
        BigDecimal cmvValue = request.initialStock().add(request.purchasesInPeriod()).subtract(request.finalStock());

        // CMV% = (CMV / grossRevenue) * 100
        BigDecimal cmvPercentage = BigDecimal.ZERO;
        if (grossRevenue.compareTo(BigDecimal.ZERO) > 0) {
            cmvPercentage = cmvValue.divide(grossRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        String alertLevel = CmvAlertLevel.fromPercentage(cmvPercentage).name();

        return new GeneralCmvResponse(grossRevenue, cmvValue, cmvPercentage, alertLevel);
    }

    private LocalDate epochToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

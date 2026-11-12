package com.fidecode.beebite.application.financial;

import com.fidecode.beebite.application.financial.dto.PaymentMethodReportResponse;
import com.fidecode.beebite.domain.financial.entity.FinancialRecord;
import com.fidecode.beebite.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.beebite.domain.financial.valueobject.FinancialStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Use Case — Distribuição de vendas por forma de pagamento.
 */
@ApplicationScoped
public class GetPaymentMethodsReportUseCase {

    private final FinancialRecordRepository financialRecordRepository;

    @Inject
    public GetPaymentMethodsReportUseCase(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public PaymentMethodReportResponse execute(UUID storeId, long startEpoch, long endEpoch) {
        LocalDate startDate = epochToLocalDate(startEpoch);
        LocalDate endDate = epochToLocalDate(endEpoch);

        List<FinancialRecord> records = financialRecordRepository.findByStoreIdAndDateRange(storeId, startDate, endDate)
                .stream().filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).toList();

        Map<String, int[]> quantityMap = new LinkedHashMap<>();
        Map<String, BigDecimal> valueMap = new LinkedHashMap<>();

        for (FinancialRecord record : records) {
            String method = record.getPaymentMethod().name();
            quantityMap.computeIfAbsent(method, k -> new int[] { 0 })[0]++;
            valueMap.merge(method, record.getTotalValue(), BigDecimal::add);
        }

        List<PaymentMethodReportResponse.MethodEntry> methods = quantityMap.entrySet().stream().map(
                e -> new PaymentMethodReportResponse.MethodEntry(e.getKey(), e.getValue()[0], valueMap.get(e.getKey())))
                .sorted(Comparator.comparing(PaymentMethodReportResponse.MethodEntry::totalValue).reversed()).toList();

        return new PaymentMethodReportResponse(startEpoch, endEpoch, methods);
    }

    private LocalDate epochToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

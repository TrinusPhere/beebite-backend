package com.trinusphere.beebite.application.financial;

import com.trinusphere.beebite.application.financial.dto.SalesByProductResponse;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use Case — Ranking de vendas por produto no período. Agrupa produtos por nome, soma quantidade e valor total. Ordena
 * por valor total (desc).
 */
@ApplicationScoped
public class GetSalesByProductUseCase {

    private final FinancialRecordRepository financialRecordRepository;

    @Inject
    public GetSalesByProductUseCase(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public SalesByProductResponse execute(UUID storeId, long startEpoch, long endEpoch) {
        LocalDate startDate = epochToLocalDate(startEpoch);
        LocalDate endDate = epochToLocalDate(endEpoch);

        List<FinancialRecord> records = financialRecordRepository.findByStoreIdAndDateRange(storeId, startDate, endDate)
                .stream().filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).toList();

        // Agrega por nome de produto
        Map<String, int[]> quantityMap = new LinkedHashMap<>();
        Map<String, BigDecimal> valueMap = new LinkedHashMap<>();

        for (FinancialRecord record : records) {
            for (FinancialRecordProduct product : record.getProducts()) {
                String name = product.getProductName();
                quantityMap.computeIfAbsent(name, k -> new int[] { 0 })[0] += product.getQuantity();
                valueMap.merge(name, product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())),
                        BigDecimal::add);
            }
        }

        List<SalesByProductResponse.ProductSale> productSales = quantityMap.entrySet().stream()
                .map(e -> new SalesByProductResponse.ProductSale(e.getKey(), e.getValue()[0], valueMap.get(e.getKey())))
                .sorted(Comparator.comparing(SalesByProductResponse.ProductSale::totalValue).reversed()).toList();

        return new SalesByProductResponse(startEpoch, endEpoch, productSales);
    }

    private LocalDate epochToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

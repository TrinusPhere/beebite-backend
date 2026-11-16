package com.trinusphere.beebite.application.analytics;

import com.trinusphere.beebite.application.analytics.dto.AnalyticsOverviewResponse;
import com.trinusphere.beebite.domain.financial.repository.FinancialRecordRepository;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialStatus;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.catalog.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.*;
import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class GetAnalyticsOverviewUseCase {
    private final FinancialRecordRepository financial;
    private final OrderRepository orders;
    private final ProductRepository products;

    @Inject
    public GetAnalyticsOverviewUseCase(FinancialRecordRepository financial, OrderRepository orders,
            ProductRepository products) {
        this.financial = financial;
        this.orders = orders;
        this.products = products;
    }

    public AnalyticsOverviewResponse execute(UUID storeId, LocalDate start, LocalDate end) {
        if (start == null)
            start = LocalDate.now().withDayOfMonth(1);
        if (end == null)
            end = LocalDate.now();
        var records = financial.findByStoreIdAndDateRange(storeId, start, end).stream()
                .filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).toList();
        BigDecimal gross = records.stream().map(r -> r.getTotalValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = records.size();
        BigDecimal avg = count > 0 ? gross.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        var productIds = records.stream().flatMap(r -> r.getProducts().stream()).map(p -> p.getProductId())
                .filter(Objects::nonNull).distinct().toList();
        var productMap = new HashMap<UUID, com.trinusphere.beebite.domain.catalog.entity.Product>();
        products.findAllById(productIds).forEach(p -> productMap.put(p.getId(), p));
        BigDecimal cmv = BigDecimal.ZERO;
        for (var r : records)
            for (var rp : r.getProducts()) {
                var p = productMap.get(rp.getProductId());
                if (p != null)
                    cmv = cmv.add(p.calculateTotalCost().multiply(BigDecimal.valueOf(rp.getQuantity())));
            }
        BigDecimal margin = gross.signum() > 0
                ? gross.subtract(cmv).multiply(BigDecimal.valueOf(100)).divide(gross, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        var orderIds = records.stream().map(r -> r.getOrderId()).toList();
        var channelMap = new LinkedHashMap<String, BigDecimal>();
        var operationMap = new LinkedHashMap<String, BigDecimal>();
        var valueByOrder = new HashMap<UUID, BigDecimal>();
        records.forEach(r -> valueByOrder.put(r.getOrderId(), r.getTotalValue()));
        orders.findAllById(orderIds).forEach(o -> {
            var v = valueByOrder.getOrDefault(o.getId(), BigDecimal.ZERO);
            channelMap.merge(o.getChannel() != null ? o.getChannel() : o.getPlatform().name(), v, BigDecimal::add);
            operationMap.merge(o.getOperation() != null ? o.getOperation()
                    : (o.getType().name().equals("DELIVERY") ? "DELIVERY" : "SALAO"), v, BigDecimal::add);
        });
        var alerts = new ArrayList<String>();
        if (gross.signum() == 0)
            alerts.add("Sem vendas concluídas no período selecionado.");
        if (margin.compareTo(BigDecimal.valueOf(30)) < 0 && gross.signum() > 0)
            alerts.add("Margem bruta estimada abaixo de 30%. Revise custos e preços.");
        return new AnalyticsOverviewResponse(gross, gross, cmv, margin, avg, count, BigDecimal.ZERO, BigDecimal.ZERO,
                channelMap.entrySet().stream()
                        .map(e -> new AnalyticsOverviewResponse.Breakdown(e.getKey(), e.getValue())).toList(),
                operationMap.entrySet().stream()
                        .map(e -> new AnalyticsOverviewResponse.Breakdown(e.getKey(), e.getValue())).toList(),
                alerts);
    }
}

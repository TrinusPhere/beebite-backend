package com.fidecode.biteflow.application.dashboard;

import com.fidecode.biteflow.application.dashboard.dto.DashboardResponse;
import com.fidecode.biteflow.domain.customer.repository.CustomerRepository;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.biteflow.domain.financial.valueobject.FinancialStatus;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case — Dashboard de KPIs.
 * Consolida dados de financeiro, pedidos, clientes e entregadores.
 */
@ApplicationScoped
public class GetDashboardUseCase {

    private final FinancialRecordRepository financialRecordRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryDriverRepository deliveryDriverRepository;

    @Inject
    public GetDashboardUseCase(FinancialRecordRepository financialRecordRepository,
                                OrderRepository orderRepository,
                                CustomerRepository customerRepository,
                                DeliveryDriverRepository deliveryDriverRepository) {
        this.financialRecordRepository = financialRecordRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.deliveryDriverRepository = deliveryDriverRepository;
    }

    public DashboardResponse execute(UUID storeId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // ── KPI 1: Vendas do dia ──
        List<FinancialRecord> todayRecords = financialRecordRepository
            .findByStoreIdAndDateRange(storeId, today, today)
            .stream()
            .filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO)
            .toList();

        BigDecimal todayRevenue = todayRecords.stream()
            .map(FinancialRecord::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FinancialRecord> yesterdayRecords = financialRecordRepository
            .findByStoreIdAndDateRange(storeId, yesterday, yesterday)
            .stream()
            .filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO)
            .toList();

        BigDecimal yesterdayRevenue = yesterdayRecords.stream()
            .map(FinancialRecord::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String revenueChange = calculateChange(todayRevenue, yesterdayRevenue);

        // ── KPI 2: Novos clientes (total — comparação com ontem é por query count) ──
        long totalCustomers = customerRepository.findAllByStoreId(storeId, 0, 1).getTotalElements();

        // ── KPI 3: Pedidos ativos ──
        List<Order> activeOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId,
            today.atStartOfDay());
        int activeOrderCount = activeOrders.size();

        // ── KPI 4: Ticket médio ──
        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (!todayRecords.isEmpty()) {
            ticketMedio = todayRevenue.divide(
                BigDecimal.valueOf(todayRecords.size()), 2, RoundingMode.HALF_UP);
        }

        // Stats
        List<DashboardResponse.StatItem> stats = new ArrayList<>();
        stats.add(new DashboardResponse.StatItem(
            "Vendas Hoje", todayRevenue, formatCurrency(todayRevenue), revenueChange));
        stats.add(new DashboardResponse.StatItem(
            "Total Clientes", BigDecimal.valueOf(totalCustomers),
            String.valueOf(totalCustomers), ""));
        stats.add(new DashboardResponse.StatItem(
            "Pedidos Ativos", BigDecimal.valueOf(activeOrderCount),
            String.valueOf(activeOrderCount), ""));
        stats.add(new DashboardResponse.StatItem(
            "Ticket Médio", ticketMedio, formatCurrency(ticketMedio), ""));

        // ── Pedidos recentes (últimos 10 ativos) ──
        List<DashboardResponse.RecentOrder> recentOrders = activeOrders.stream()
            .limit(10)
            .map(order -> new DashboardResponse.RecentOrder(
                order.getDisplayId(),
                order.getCustomerName(),
                buildItemsSummary(order.getItems()),
                order.getTotalValue(),
                order.getStatus().name(),
                order.getCreatedAt()
            ))
            .toList();

        // ── Equipe de entrega ──
        List<DashboardResponse.DriverStatus> deliveryTeam = deliveryDriverRepository
            .findAllByStoreId(storeId, 0, 100)
            .getContent()
            .stream()
            .map(driver -> new DashboardResponse.DriverStatus(
                driver.getId(),
                driver.getName(),
                driver.getStatus().name()
            ))
            .toList();

        return new DashboardResponse(stats, recentOrders, deliveryTeam, LocalDateTime.now());
    }

    private String calculateChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? "0%" : "+100%";
        }
        BigDecimal change = current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(1, RoundingMode.HALF_UP);
        String prefix = change.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + change.stripTrailingZeros().toPlainString() + "%";
    }

    private String formatCurrency(BigDecimal value) {
        return "R$ " + value.setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
            .replace(".", ",");
    }

    private String buildItemsSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "";
        return items.stream()
            .map(item -> item.getQuantity() + "x " + item.getProductName())
            .collect(Collectors.joining(", "));
    }
}

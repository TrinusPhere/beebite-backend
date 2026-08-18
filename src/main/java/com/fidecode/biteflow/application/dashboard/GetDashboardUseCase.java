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

        // ── KPI 2: Novos clientes (hoje vs ontem) ──
        long newCustomersToday = customerRepository.countByStoreIdAndCreatedSince(storeId, today.atStartOfDay());
        long newCustomersYesterday = customerRepository.countByStoreIdAndCreatedSince(storeId, yesterday.atStartOfDay())
            - newCustomersToday; // subtrai os de hoje para isolar apenas os de ontem
        String customersChange = calculateChange(
            BigDecimal.valueOf(newCustomersToday), BigDecimal.valueOf(Math.max(newCustomersYesterday, 0)));

        // ── KPI 3: Pedidos ativos (hoje vs ontem) ──
        List<Order> activeOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId,
            today.atStartOfDay());
        int activeOrderCount = activeOrders.size();

        List<Order> yesterdayActiveOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId,
            yesterday.atStartOfDay());
        // Pedidos ativos de ontem = total encontrados desde ontem - os de hoje
        int yesterdayActiveCount = Math.max(yesterdayActiveOrders.size() - activeOrderCount, 0);
        String ordersChange = calculateChange(
            BigDecimal.valueOf(activeOrderCount), BigDecimal.valueOf(yesterdayActiveCount));

        // ── KPI 4: Ticket médio (hoje vs ontem) ──
        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (!todayRecords.isEmpty()) {
            ticketMedio = todayRevenue.divide(
                BigDecimal.valueOf(todayRecords.size()), 2, RoundingMode.HALF_UP);
        }
        BigDecimal yesterdayTicketMedio = BigDecimal.ZERO;
        if (!yesterdayRecords.isEmpty()) {
            yesterdayTicketMedio = yesterdayRevenue.divide(
                BigDecimal.valueOf(yesterdayRecords.size()), 2, RoundingMode.HALF_UP);
        }
        String ticketChange = calculateChange(ticketMedio, yesterdayTicketMedio);

        // Stats
        List<DashboardResponse.StatItem> stats = new ArrayList<>();
        stats.add(new DashboardResponse.StatItem(
            "Vendas Hoje", todayRevenue, formatCurrency(todayRevenue), revenueChange));
        stats.add(new DashboardResponse.StatItem(
            "Novos Clientes", BigDecimal.valueOf(newCustomersToday),
            String.valueOf(newCustomersToday), customersChange));
        stats.add(new DashboardResponse.StatItem(
            "Pedidos Ativos", BigDecimal.valueOf(activeOrderCount),
            String.valueOf(activeOrderCount), ordersChange));
        stats.add(new DashboardResponse.StatItem(
            "Ticket Médio", ticketMedio, formatCurrency(ticketMedio), ticketChange));

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

package com.fidecode.beebite.application.dashboard;

import com.fidecode.beebite.application.dashboard.dto.DashboardResponse;
import com.fidecode.beebite.domain.customer.repository.CustomerRepository;
import com.fidecode.beebite.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.beebite.domain.financial.entity.FinancialRecord;
import com.fidecode.beebite.domain.financial.repository.FinancialRecordRepository;
import com.fidecode.beebite.domain.financial.valueobject.FinancialStatus;
import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.entity.OrderItem;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case — Dashboard de KPIs. Consolida dados de financeiro, pedidos, clientes e entregadores.
 */
@ApplicationScoped
public class GetDashboardUseCase {

    private final FinancialRecordRepository financialRecordRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryDriverRepository deliveryDriverRepository;

    @Inject
    public GetDashboardUseCase(FinancialRecordRepository financialRecordRepository, OrderRepository orderRepository,
            CustomerRepository customerRepository, DeliveryDriverRepository deliveryDriverRepository) {
        this.financialRecordRepository = financialRecordRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.deliveryDriverRepository = deliveryDriverRepository;
    }

    public DashboardResponse execute(UUID storeId) {
        return execute(storeId, "daily", null, null);
    }

    public DashboardResponse execute(UUID storeId, String period, LocalDate customStartDate, LocalDate customEndDate) {
        String p = period != null ? period.toLowerCase().trim() : "daily";
        LocalDate today = LocalDate.now();

        LocalDate start;
        LocalDate end;
        LocalDate prevStart;
        LocalDate prevEnd;
        String salesLabel;

        switch (p) {
            case "weekly":
                start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = today;
                prevStart = start.minusWeeks(1);
                prevEnd = prevStart.plusDays(ChronoUnit.DAYS.between(start, end));
                salesLabel = "Vendas da Semana";
                break;
            case "monthly":
                start = today.withDayOfMonth(1);
                end = today;
                prevStart = start.minusMonths(1);
                prevEnd = prevStart.plusDays(ChronoUnit.DAYS.between(start, end));
                salesLabel = "Vendas do Mês";
                break;
            case "custom":
                start = customStartDate != null ? customStartDate : today;
                end = customEndDate != null ? customEndDate : start;
                if (end.isBefore(start)) {
                    LocalDate tmp = start;
                    start = end;
                    end = tmp;
                }
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                prevEnd = start.minusDays(1);
                prevStart = prevEnd.minusDays(days - 1);
                salesLabel = "Vendas no Período";
                break;
            case "daily":
            default:
                start = customStartDate != null ? customStartDate : today;
                end = start;
                prevStart = start.minusDays(1);
                prevEnd = prevStart;
                salesLabel = start.equals(today) ? "Vendas Hoje" : "Vendas do Dia";
                break;
        }

        // ── KPI 1: Vendas ──
        List<FinancialRecord> currentRecords = financialRecordRepository
                .findByStoreIdAndDateRange(storeId, start, end)
                .stream().filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).toList();

        BigDecimal currentRevenue = currentRecords.stream()
                .map(FinancialRecord::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FinancialRecord> prevRecords = financialRecordRepository
                .findByStoreIdAndDateRange(storeId, prevStart, prevEnd)
                .stream().filter(r -> r.getStatus() == FinancialStatus.CONCLUIDO).toList();

        BigDecimal prevRevenue = prevRecords.stream()
                .map(FinancialRecord::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String revenueChange = calculateChange(currentRevenue, prevRevenue);

        // ── KPI 2: Novos clientes ──
        long newCustomersCurrent;
        long newCustomersPrev;

        if (p.equals("daily") && start.equals(today)) {
            newCustomersCurrent = customerRepository.countByStoreIdAndCreatedSince(storeId, today.atStartOfDay());
            newCustomersPrev = Math.max(0,
                    customerRepository.countByStoreIdAndCreatedSince(storeId, today.minusDays(1).atStartOfDay())
                            - newCustomersCurrent);
        } else {
            newCustomersCurrent = customerRepository.countByStoreIdAndCreatedBetween(storeId, start.atStartOfDay(),
                    end.atTime(23, 59, 59, 999999999));
            newCustomersPrev = customerRepository.countByStoreIdAndCreatedBetween(storeId, prevStart.atStartOfDay(),
                    prevEnd.atTime(23, 59, 59, 999999999));
        }

        String customersChange = calculateChange(BigDecimal.valueOf(newCustomersCurrent),
                BigDecimal.valueOf(newCustomersPrev));

        // ── KPI 3: Pedidos ──
        List<Order> currentOrders;
        int currentOrderCount;
        int prevOrderCount;
        String ordersLabel;

        if (p.equals("daily") && start.equals(today)) {
            currentOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId, today.atStartOfDay());
            currentOrderCount = currentOrders.size();
            List<Order> yesterdayOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId,
                    today.minusDays(1).atStartOfDay());
            prevOrderCount = Math.max(yesterdayOrders.size() - currentOrderCount, 0);
            ordersLabel = "Pedidos Ativos";
        } else {
            currentOrders = orderRepository.findByStoreIdAndDateRange(storeId, start.atStartOfDay(),
                    end.atTime(23, 59, 59, 999999999));
            currentOrderCount = currentOrders.size();
            List<Order> prevPeriodOrders = orderRepository.findByStoreIdAndDateRange(storeId, prevStart.atStartOfDay(),
                    prevEnd.atTime(23, 59, 59, 999999999));
            prevOrderCount = prevPeriodOrders.size();
            ordersLabel = "Total Pedidos";
        }

        String ordersChange = calculateChange(BigDecimal.valueOf(currentOrderCount),
                BigDecimal.valueOf(prevOrderCount));

        // ── KPI 4: Ticket médio ──
        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (!currentRecords.isEmpty()) {
            ticketMedio = currentRevenue.divide(BigDecimal.valueOf(currentRecords.size()), 2, RoundingMode.HALF_UP);
        }
        BigDecimal prevTicketMedio = BigDecimal.ZERO;
        if (!prevRecords.isEmpty()) {
            prevTicketMedio = prevRevenue.divide(BigDecimal.valueOf(prevRecords.size()), 2, RoundingMode.HALF_UP);
        }
        String ticketChange = calculateChange(ticketMedio, prevTicketMedio);

        // Stats
        List<DashboardResponse.StatItem> stats = new ArrayList<>();
        stats.add(new DashboardResponse.StatItem(salesLabel, currentRevenue, formatCurrency(currentRevenue),
                revenueChange));
        stats.add(new DashboardResponse.StatItem("Novos Clientes", BigDecimal.valueOf(newCustomersCurrent),
                String.valueOf(newCustomersCurrent), customersChange));
        stats.add(new DashboardResponse.StatItem(ordersLabel, BigDecimal.valueOf(currentOrderCount),
                String.valueOf(currentOrderCount), ordersChange));
        stats.add(new DashboardResponse.StatItem("Ticket Médio", ticketMedio, formatCurrency(ticketMedio), ticketChange));

        // ── Pedidos recentes (últimos 10 do período) ──
        List<DashboardResponse.RecentOrder> recentOrders = currentOrders.stream().limit(10)
                .map(order -> new DashboardResponse.RecentOrder(order.getDisplayId(), order.getCustomerName(),
                        buildItemsSummary(order.getItems()), order.getTotalValue(), order.getStatus().name(),
                        order.getCreatedAt()))
                .toList();

        // ── Equipe de entrega ──
        List<DashboardResponse.DriverStatus> deliveryTeam = deliveryDriverRepository.findAllByStoreId(storeId, 0, 100)
                .getContent().stream().map(driver -> new DashboardResponse.DriverStatus(driver.getId(),
                        driver.getName(), driver.getStatus().name()))
                .toList();

        return new DashboardResponse(stats, recentOrders, deliveryTeam, LocalDateTime.now());
    }

    private String calculateChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? "0%" : "+100%";
        }
        BigDecimal change = current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
        String prefix = change.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + change.stripTrailingZeros().toPlainString() + "%";
    }

    private String formatCurrency(BigDecimal value) {
        return "R$ " + value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace(".", ",");
    }

    private String buildItemsSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty())
            return "";
        return items.stream().map(item -> item.getQuantity() + "x " + item.getProductName())
                .collect(Collectors.joining(", "));
    }
}

package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderKanbanResponse;
import com.fidecode.biteflow.domain.cash.entity.CashSession;
import com.fidecode.biteflow.domain.cash.repository.CashSessionRepository;
import com.fidecode.biteflow.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetOrderKanbanUseCase {

    private final OrderRepository orderRepository;
    private final CashSessionRepository cashSessionRepository;

    public GetOrderKanbanUseCase(OrderRepository orderRepository) {
        this(orderRepository, null);
    }

    @Inject
    public GetOrderKanbanUseCase(OrderRepository orderRepository, CashSessionRepository cashSessionRepository) {
        this.orderRepository = orderRepository;
        this.cashSessionRepository = cashSessionRepository;
    }

    public OrderKanbanResponse listKanban(UUID storeId, String typeFilter) {
        List<CashSession> openSessions = cashSessionRepository != null
                ? cashSessionRepository.findByStoreIdAndStatus(storeId, CashSessionStatus.OPEN)
                : List.of();

        // Se o repositório de caixa estiver disponível e o caixa estiver encerrado, o kanban fica limpo
        if (cashSessionRepository != null && openSessions.isEmpty()) {
            return new OrderKanbanResponse(List.of(
                    buildColumn("ANALISE", "Pedido em Análise", "Search", List.of()),
                    buildColumn("PREPARO", "Em Preparo", "ChefHat", List.of()),
                    buildColumn("EM_ROTA", "Saiu para Entrega", "Truck", List.of()),
                    buildColumn("ENTREGUE", "Entregue", "PackageCheck", List.of()),
                    buildColumn("FINALIZADO", "Finalizado", "CheckCircle2", List.of())
            ));
        }

        // Se houver sessão aberta, busca apenas pedidos criados a partir da abertura daquele caixa específico
        LocalDateTime since = openSessions.isEmpty()
                ? java.time.LocalDate.now().atStartOfDay()
                : openSessions.stream().map(CashSession::getOpenedAt).min(LocalDateTime::compareTo).get();

        List<Order> activeOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId, since);

        if (typeFilter != null && !typeFilter.isBlank()) {
            Set<String> types = Arrays.stream(typeFilter.split(",")).map(String::trim).map(String::toUpperCase)
                    .collect(Collectors.toSet());
            activeOrders = activeOrders.stream().filter(o -> types.contains(o.getType().name())).toList();
        }

        Map<OrderStatus, List<Order>> grouped = activeOrders.stream().collect(Collectors.groupingBy(Order::getStatus));

        List<Order> finishedAndCancelled = new java.util.ArrayList<>();
        finishedAndCancelled.addAll(grouped.getOrDefault(OrderStatus.FINALIZADO, List.of()));
        finishedAndCancelled.addAll(grouped.getOrDefault(OrderStatus.CANCELADO, List.of()));
        finishedAndCancelled.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        List<OrderKanbanResponse.KanbanColumn> columns = List.of(
                buildColumn("ANALISE", "Pedido em Análise", "Search",
                        grouped.getOrDefault(OrderStatus.ANALISE, List.of())),
                buildColumn("PREPARO", "Em Preparo", "ChefHat", grouped.getOrDefault(OrderStatus.PREPARO, List.of())),
                buildColumn("EM_ROTA", "Saiu para Entrega", "Truck",
                        grouped.getOrDefault(OrderStatus.EM_ROTA, List.of())),
                buildColumn("ENTREGUE", "Entregue", "PackageCheck",
                        grouped.getOrDefault(OrderStatus.ENTREGUE, List.of())),
                buildColumn("FINALIZADO", "Finalizado", "CheckCircle2",
                        finishedAndCancelled));

        return new OrderKanbanResponse(columns);
    }

    private OrderKanbanResponse.KanbanColumn buildColumn(String id, String title, String icon, List<Order> orders) {
        List<OrderKanbanResponse.KanbanOrder> kanbanOrders = orders
                .stream().map(
                        o -> new OrderKanbanResponse.KanbanOrder(o.getId().toString(), o.getDisplayId(),
                                o.getCustomerName(), o.getCustomerPhone(), buildItemsSummary(o), o.getTotalValue(),
                                o.getStatus().name(), o.getType().name(), o.getOperation(), o.getChannel(),
                                o.getServiceType(), o.getSource(), o.getDriverName(), o.getPlatform().name(),
                                o.getExternalOrderId(), o.isNfeEmitted(),
                                o.getCreatedAt() != null
                                        ? o.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null))
                .toList();
        return new OrderKanbanResponse.KanbanColumn(id, title, icon, kanbanOrders);
    }

    private String buildItemsSummary(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty())
            return "";
        return order.getItems().stream().map(i -> i.getQuantity() + "x " + i.getProductName())
                .collect(Collectors.joining(",\n"));
    }
}

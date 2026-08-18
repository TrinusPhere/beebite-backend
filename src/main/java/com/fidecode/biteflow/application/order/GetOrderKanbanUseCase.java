package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderKanbanResponse;
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

    @Inject
    public GetOrderKanbanUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderKanbanResponse listKanban(UUID storeId, String typeFilter) {
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        List<Order> activeOrders = orderRepository.findActiveKanbanOrdersByStoreId(storeId, startOfDay);

        if (typeFilter != null && !typeFilter.isBlank()) {
            Set<String> types = Arrays.stream(typeFilter.split(","))
                .map(String::trim).map(String::toUpperCase)
                .collect(Collectors.toSet());
            activeOrders = activeOrders.stream()
                .filter(o -> types.contains(o.getType().name()))
                .toList();
        }

        Map<OrderStatus, List<Order>> grouped = activeOrders.stream()
            .collect(Collectors.groupingBy(Order::getStatus));

        List<OrderKanbanResponse.KanbanColumn> columns = List.of(
            buildColumn("ANALISE", "Pedido em Análise", "Search", grouped.getOrDefault(OrderStatus.ANALISE, List.of())),
            buildColumn("PREPARO", "Em Preparo", "ChefHat", grouped.getOrDefault(OrderStatus.PREPARO, List.of())),
            buildColumn("EM_ROTA", "Saiu para Entrega", "Truck", grouped.getOrDefault(OrderStatus.EM_ROTA, List.of())),
            buildColumn("FINALIZADO", "Finalizado", "CheckCircle2", grouped.getOrDefault(OrderStatus.FINALIZADO, List.of()))
        );

        return new OrderKanbanResponse(columns);
    }

    private OrderKanbanResponse.KanbanColumn buildColumn(String id, String title, String icon, List<Order> orders) {
        List<OrderKanbanResponse.KanbanOrder> kanbanOrders = orders.stream()
            .map(o -> new OrderKanbanResponse.KanbanOrder(
                o.getId().toString(),
                o.getDisplayId(),
                o.getCustomerName(),
                buildItemsSummary(o),
                o.getTotalValue(),
                o.getType().name(),
                o.getDriverName(),
                o.getPlatform().name(),
                o.getExternalOrderId(),
                o.isNfeEmitted(),
                o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            )).toList();
        return new OrderKanbanResponse.KanbanColumn(id, title, icon, kanbanOrders);
    }

    private String buildItemsSummary(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) return "";
        return order.getItems().stream()
            .map(i -> i.getQuantity() + "x " + i.getProductName())
            .collect(Collectors.joining(", "));
    }
}

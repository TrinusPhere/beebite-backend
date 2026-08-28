package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.*;
import com.fidecode.biteflow.application.order.port.BiteflowEventPublisher;
import com.fidecode.biteflow.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderPlatform;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class ManageOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final Event<OrderFinalizedEvent> orderFinalizedEvent;
    private final BiteflowEventPublisher eventPublisher;

    @Inject
    public ManageOrderUseCase(OrderRepository orderRepository,
                              OrderDomainService orderDomainService,
                              Event<OrderFinalizedEvent> orderFinalizedEvent,
                              BiteflowEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.orderFinalizedEvent = orderFinalizedEvent;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse advanceStatus(UUID storeId, UUID orderId) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
            storeId
        );
        orderDomainService.advanceStatus(order);
        Order saved = orderRepository.save(order);
        if (saved.getStatus() == OrderStatus.FINALIZADO) {
            fireOrderFinalizedEvent(saved);
        }
        publishIfoodEventIfNeeded(saved);
        return OrderDtoMapper.toResponse(saved);
    }

    public OrderResponse moveToColumn(UUID storeId, UUID orderId, String targetStatus) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
            storeId
        );
        OrderStatus target = OrderStatus.valueOf(targetStatus);
        orderDomainService.moveToColumn(order, target);
        Order saved = orderRepository.save(order);
        if (saved.getStatus() == OrderStatus.FINALIZADO) {
            fireOrderFinalizedEvent(saved);
        }
        publishIfoodEventIfNeeded(saved);
        return OrderDtoMapper.toResponse(saved);
    }

    public OrderResponse assignDriver(UUID storeId, UUID orderId, AssignDriverRequest request) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
            storeId
        );
        UUID driverId = UUID.fromString(request.driverId());
        orderDomainService.assignDriver(order, driverId, request.driverName());
        return OrderDtoMapper.toResponse(orderRepository.save(order));
    }

    @jakarta.transaction.Transactional
    public void batchMove(UUID storeId, List<String> orderIds, MoveOrderRequest request) {
        if (orderIds == null || orderIds.isEmpty()) return;
        OrderStatus target = OrderStatus.valueOf(request.targetStatus());
        List<UUID> ids = orderIds.stream().map(UUID::fromString).toList();

        List<Order> orders = orderRepository.findAllById(ids);
        if (orders.size() != ids.size()) {
            throw new IllegalArgumentException("Alguns pedidos não foram encontrados para atualização em lote.");
        }

        for (Order order : orders) {
            StoreGuard.enforce(order, storeId);
            orderDomainService.moveToColumn(order, target);
            orderRepository.save(order);
        }
    }

    @jakarta.transaction.Transactional
    public void batchAssignDriver(UUID storeId, AssignDriverRequest request) {
        if (request.orderIds() == null || request.orderIds().isEmpty()) return;
        UUID driverId = UUID.fromString(request.driverId());
        List<UUID> ids = request.orderIds().stream().map(UUID::fromString).toList();

        List<Order> orders = orderRepository.findAllById(ids);
        if (orders.size() != ids.size()) {
            throw new IllegalArgumentException("Alguns pedidos não foram encontrados para atribuição em lote.");
        }

        for (Order order : orders) {
            StoreGuard.enforce(order, storeId);
            orderDomainService.assignDriver(order, driverId, request.driverName());
            orderRepository.save(order);
        }
    }

    public NfeResponse emitNfe(UUID storeId, UUID orderId) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
            storeId
        );
        order.markAsNfeEmitted();
        orderRepository.save(order);

        return new NfeResponse(
            orderId.toString(),
            String.format("%09d", new Random().nextInt(999999999)),
            "35260812345678000195550010001234561234567890",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "AUTORIZADA"
        );
    }

    /**
     * Publica evento no Pub/Sub apenas se o pedido veio do iFood.
     * O publisher (infra) ignora se Pub/Sub não estiver configurado.
     */
    private void publishIfoodEventIfNeeded(Order order) {
        if (order.getPlatform() == OrderPlatform.IFOOD && order.getExternalOrderId() != null) {
            if (order.getStatus() == OrderStatus.CANCELADO) {
                eventPublisher.publishCancelled(order, "501", "Cancelado pelo operador");
            } else {
                eventPublisher.publishStatusChanged(order);
            }
        }
    }

    private void fireOrderFinalizedEvent(Order order) {
        List<OrderFinalizedEvent.ProductSnapshot> products = order.getItems().stream()
            .map(item -> new OrderFinalizedEvent.ProductSnapshot(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice()
            ))
            .toList();

        orderFinalizedEvent.fire(new OrderFinalizedEvent(
            order.getId(),
            order.getStoreId(),
            order.getTotalValue(),
            order.getPaymentMethod(),
            order.getCustomerId(),
            order.getDriverId(),
            products
        ));
    }
}

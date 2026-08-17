package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.*;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
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

    @Inject
    public ManageOrderUseCase(OrderRepository orderRepository, OrderDomainService orderDomainService) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
    }

    public OrderResponse advanceStatus(UUID storeId, UUID orderId) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
            storeId
        );
        orderDomainService.advanceStatus(order);
        return OrderDtoMapper.toResponse(orderRepository.save(order));
    }

    public OrderResponse moveToColumn(UUID storeId, UUID orderId, String targetStatus) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId.toString())),
            storeId
        );
        OrderStatus target = OrderStatus.valueOf(targetStatus);
        orderDomainService.moveToColumn(order, target);
        return OrderDtoMapper.toResponse(orderRepository.save(order));
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
}

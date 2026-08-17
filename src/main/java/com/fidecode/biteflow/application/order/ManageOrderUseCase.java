package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.*;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
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
    public ManageOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.orderDomainService = new OrderDomainService();
    }

    public OrderResponse advanceStatus(UUID storeId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        orderDomainService.advanceStatus(order);
        return OrderDtoMapper.toResponse(orderRepository.save(order));
    }

    public OrderResponse moveToColumn(UUID storeId, UUID orderId, String targetStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        OrderStatus target = OrderStatus.valueOf(targetStatus);
        orderDomainService.moveToColumn(order, target);
        return OrderDtoMapper.toResponse(orderRepository.save(order));
    }

    public OrderResponse assignDriver(UUID storeId, UUID orderId, AssignDriverRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
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
            if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado a um dos pedidos");
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
            if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado a um dos pedidos");
            orderDomainService.assignDriver(order, driverId, request.driverName());
            orderRepository.save(order);
        }
    }

    public NfeResponse emitNfe(UUID storeId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
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

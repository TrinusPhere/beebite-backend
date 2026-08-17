package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Inject
    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse getById(UUID storeId, UUID id) {
        Order order = StoreGuard.enforce(
            orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
            storeId
        );
        return OrderDtoMapper.toResponse(order);
    }
}

package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
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
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id.toString()));
        if (!order.getStoreId().equals(storeId)) throw new com.fidecode.biteflow.domain.shared.exception.ForbiddenAccessException("Acesso negado");
        return OrderDtoMapper.toResponse(order);
    }
}

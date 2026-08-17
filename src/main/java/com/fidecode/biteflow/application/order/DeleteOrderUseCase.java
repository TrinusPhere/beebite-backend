package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;

    @Inject
    public DeleteOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void delete(UUID storeId, UUID id) {
        StoreGuard.enforce(
            orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
            storeId
        );
        orderRepository.deleteById(id);
    }
}

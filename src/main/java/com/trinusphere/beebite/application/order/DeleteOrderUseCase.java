package com.trinusphere.beebite.application.order;

import com.trinusphere.beebite.application.kds.CancelProductionTasksByOrderUseCase;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.exception.OrderNotFoundException;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;
    private final CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase;

    @Inject
    public DeleteOrderUseCase(OrderRepository orderRepository,
                             CancelProductionTasksByOrderUseCase cancelProductionTasksByOrderUseCase) {
        this.orderRepository = orderRepository;
        this.cancelProductionTasksByOrderUseCase = cancelProductionTasksByOrderUseCase;
    }

    public void delete(UUID storeId, UUID id) {
        StoreGuard.enforce(orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
                storeId);
        if (cancelProductionTasksByOrderUseCase != null) {
            cancelProductionTasksByOrderUseCase.execute(storeId, id);
        }
        orderRepository.deleteById(id);
    }
}

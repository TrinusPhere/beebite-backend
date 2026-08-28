package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.application.shared.port.TransactionExecutor;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class MoveOrderToColumnUseCase {
    private final OrderRepository repo;
    private final OrderDomainService service;
    private final OrderFinalizationPublisher publisher;
    private final TransactionExecutor tx;

    @Inject
    public MoveOrderToColumnUseCase(OrderRepository repo, OrderDomainService service,
            OrderFinalizationPublisher publisher, TransactionExecutor tx) {
        this.repo = repo;
        this.service = service;
        this.publisher = publisher;
        this.tx = tx;
    }

    public OrderResponse execute(UUID storeId, UUID id, String targetStatus) {
        return tx.execute(() -> {
            var order = StoreGuard
                    .enforce(repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())), storeId);
            service.moveToColumn(order, OrderStatus.valueOf(targetStatus));
            var saved = repo.save(order);
            publisher.publishIfFinalized(saved);
            return OrderDtoMapper.toResponse(saved);
        });
    }
}

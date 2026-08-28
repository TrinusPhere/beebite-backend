package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.*;
import com.fidecode.biteflow.domain.order.exception.OrderNotFoundException;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.service.OrderDomainService;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class AssignOrderDriverUseCase {
    private final OrderRepository repo;
    private final OrderDomainService service;

    @Inject
    public AssignOrderDriverUseCase(OrderRepository repo, OrderDomainService service) {
        this.repo = repo;
        this.service = service;
    }

    public OrderResponse execute(UUID storeId, UUID id, AssignDriverRequest request) {
        var order = StoreGuard.enforce(repo.findById(id).orElseThrow(() -> new OrderNotFoundException(id.toString())),
                storeId);
        service.assignDriver(order, UUID.fromString(request.driverId()), request.driverName());
        return OrderDtoMapper.toResponse(repo.save(order));
    }
}

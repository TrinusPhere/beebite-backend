package com.fidecode.beebite.application.order;

import com.fidecode.beebite.application.order.dto.AssignDriverRequest;
import com.fidecode.beebite.application.shared.port.TransactionExecutor;
import com.fidecode.beebite.domain.order.exception.BatchOrderNotFoundException;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.order.service.OrderDomainService;
import com.fidecode.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class BatchAssignOrderDriverUseCase {
    private final OrderRepository repo;
    private final OrderDomainService service;
    private final TransactionExecutor tx;

    @Inject
    public BatchAssignOrderDriverUseCase(OrderRepository repo, OrderDomainService service, TransactionExecutor tx) {
        this.repo = repo;
        this.service = service;
        this.tx = tx;
    }

    public void execute(UUID storeId, AssignDriverRequest request) {
        if (request.orderIds() == null || request.orderIds().isEmpty())
            return;
        tx.execute(() -> {
            var ids = request.orderIds().stream().map(UUID::fromString).toList();
            var orders = repo.findAllById(ids);
            if (orders.size() != ids.size())
                throw new BatchOrderNotFoundException();
            var driverId = UUID.fromString(request.driverId());
            for (var order : orders) {
                StoreGuard.enforce(order, storeId);
                service.assignDriver(order, driverId, request.driverName());
                repo.save(order);
            }
            return null;
        });
    }
}

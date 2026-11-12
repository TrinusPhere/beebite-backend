package com.fidecode.beebite.application.kds;

import com.fidecode.beebite.application.kds.port.KdsEventPublisher;
import com.fidecode.beebite.domain.kds.repository.ProductionTaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CancelProductionTasksByOrderUseCase {

    private final ProductionTaskRepository taskRepository;
    private final KdsEventPublisher eventPublisher;

    @Inject
    public CancelProductionTasksByOrderUseCase(ProductionTaskRepository taskRepository,
                                               KdsEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(UUID storeId, UUID orderId) {
        taskRepository.cancelAllByOrderId(orderId);
        if (eventPublisher != null) {
            eventPublisher.publishTasksCanceled(storeId, orderId);
        }
    }
}


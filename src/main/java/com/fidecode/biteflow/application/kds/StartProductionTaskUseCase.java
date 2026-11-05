package com.fidecode.biteflow.application.kds;

import com.fidecode.biteflow.application.kds.dto.ProductionTaskDtoMapper;
import com.fidecode.biteflow.application.kds.dto.ProductionTaskResponse;
import com.fidecode.biteflow.application.kds.port.KdsEventPublisher;
import com.fidecode.biteflow.domain.kds.entity.ProductionTask;
import com.fidecode.biteflow.domain.kds.exception.ProductionTaskNotFoundException;
import com.fidecode.biteflow.domain.kds.repository.ProductionTaskRepository;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class StartProductionTaskUseCase {

    private final ProductionTaskRepository taskRepository;
    private final KdsEventPublisher eventPublisher;

    @Inject
    public StartProductionTaskUseCase(ProductionTaskRepository taskRepository, KdsEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    public ProductionTaskResponse execute(UUID storeId, UUID taskId) {
        ProductionTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ProductionTaskNotFoundException(taskId.toString()));
        StoreGuard.enforce(task, storeId);

        task.startPreparation();
        ProductionTask saved = taskRepository.save(task);
        ProductionTaskResponse response = ProductionTaskDtoMapper.toResponse(saved);

        if (eventPublisher != null) {
            eventPublisher.publishTaskUpdated(storeId, response);
        }

        return response;
    }
}


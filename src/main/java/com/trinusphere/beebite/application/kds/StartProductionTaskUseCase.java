package com.trinusphere.beebite.application.kds;

import com.trinusphere.beebite.application.kds.dto.ProductionTaskDtoMapper;
import com.trinusphere.beebite.application.kds.dto.ProductionTaskResponse;
import com.trinusphere.beebite.application.kds.port.KdsEventPublisher;
import com.trinusphere.beebite.domain.kds.entity.ProductionTask;
import com.trinusphere.beebite.domain.kds.exception.ProductionTaskNotFoundException;
import com.trinusphere.beebite.domain.kds.repository.ProductionTaskRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
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


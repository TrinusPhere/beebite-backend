package com.fidecode.biteflow.application.kds.port;

import com.fidecode.biteflow.application.kds.dto.ProductionTaskResponse;

import java.util.List;
import java.util.UUID;

public interface KdsEventPublisher {
    void publishTasksDispatched(UUID storeId, List<ProductionTaskResponse> tasks);
    void publishTaskUpdated(UUID storeId, ProductionTaskResponse task);
    void publishTasksCanceled(UUID storeId, UUID orderId);
}


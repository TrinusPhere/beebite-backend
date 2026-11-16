package com.trinusphere.beebite.application.kds.port;

import com.trinusphere.beebite.application.kds.dto.ProductionTaskResponse;

import java.util.List;
import java.util.UUID;

public interface KdsEventPublisher {
    void publishTasksDispatched(UUID storeId, List<ProductionTaskResponse> tasks);
    void publishTaskUpdated(UUID storeId, ProductionTaskResponse task);
    void publishTasksCanceled(UUID storeId, UUID orderId);
}


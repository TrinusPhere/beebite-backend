package com.trinusphere.beebite.infrastructure.kds;

import com.trinusphere.beebite.application.kds.dto.ProductionTaskResponse;
import com.trinusphere.beebite.application.kds.port.KdsEventPublisher;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class KdsEventBroadcaster implements KdsEventPublisher {

    private final BroadcastProcessor<KdsEventPayload> processor = BroadcastProcessor.create();

    @Override
    public void publishTasksDispatched(UUID storeId, List<ProductionTaskResponse> tasks) {
        processor.onNext(new KdsEventPayload(storeId, "TASKS_DISPATCHED", tasks));
    }

    @Override
    public void publishTaskUpdated(UUID storeId, ProductionTaskResponse task) {
        processor.onNext(new KdsEventPayload(storeId, "TASK_UPDATED", task));
    }

    @Override
    public void publishTasksCanceled(UUID storeId, UUID orderId) {
        processor.onNext(new KdsEventPayload(storeId, "TASKS_CANCELED", orderId.toString()));
    }

    public Multi<KdsEventPayload> getStream(UUID storeId) {
        return processor.filter(payload -> payload.storeId().equals(storeId));
    }
}

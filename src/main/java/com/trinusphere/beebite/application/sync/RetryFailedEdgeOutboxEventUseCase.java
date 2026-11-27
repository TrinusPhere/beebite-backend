package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.port.EdgeOutboxRequeuePort;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.exception.EdgeOutboxEventNotFoundException;
import com.trinusphere.beebite.domain.sync.exception.EdgeOutboxEventNotRetryableException;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RetryFailedEdgeOutboxEventUseCase {

    private final EdgeOutboxRepository outboxRepository;
    private final EdgeOutboxRequeuePort requeuePort;

    @Inject
    public RetryFailedEdgeOutboxEventUseCase(
            EdgeOutboxRepository outboxRepository,
            EdgeOutboxRequeuePort requeuePort) {
        this.outboxRepository = outboxRepository;
        this.requeuePort = requeuePort;
    }

    public int execute(UUID storeId, UUID eventId) {
        EdgeOutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow(() -> new EdgeOutboxEventNotFoundException(eventId));
        StoreGuard.enforce(event, storeId);

        if (event.getStatus() != EdgeOutboxStatus.FAILED) {
            throw new EdgeOutboxEventNotRetryableException(eventId);
        }

        return requeuePort.requeueByEventType(storeId, event.getEventType(), event.getAggregateId());
    }
}


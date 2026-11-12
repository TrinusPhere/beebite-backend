package com.fidecode.beebite.application.sync;

import com.fidecode.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.fidecode.beebite.domain.sync.repository.EdgeOutboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EnqueueEdgeOutboxUseCase {

    private final EdgeOutboxRepository edgeOutboxRepository;
    private final String syncMode;

    @Inject
    public EnqueueEdgeOutboxUseCase(
            EdgeOutboxRepository edgeOutboxRepository,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode) {
        this.edgeOutboxRepository = edgeOutboxRepository;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
    }

    public Optional<EdgeOutboxEvent> execute(UUID storeId, String eventType, UUID aggregateId, String payloadJson) {
        if (!"EDGE".equals(syncMode)) {
            return Optional.empty();
        }
        EdgeOutboxEvent event = new EdgeOutboxEvent(storeId, eventType, aggregateId, payloadJson);
        return Optional.of(edgeOutboxRepository.save(event));
    }
}

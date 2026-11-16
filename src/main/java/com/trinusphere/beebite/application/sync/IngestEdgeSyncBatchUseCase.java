package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeSnapshotApplier;
import com.trinusphere.beebite.domain.sync.exception.InvalidEdgeDeviceTokenException;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IngestEdgeSyncBatchUseCase {

    private final StoreRepository storeRepository;
    private final EdgeOutboxRepository edgeOutboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final EdgeSnapshotApplier edgeSnapshotApplier;

    @Inject
    public IngestEdgeSyncBatchUseCase(
            StoreRepository storeRepository,
            EdgeOutboxRepository edgeOutboxRepository,
            PasswordEncoder passwordEncoder,
            EdgeSnapshotApplier edgeSnapshotApplier) {
        this.storeRepository = storeRepository;
        this.edgeOutboxRepository = edgeOutboxRepository;
        this.passwordEncoder = passwordEncoder;
        this.edgeSnapshotApplier = edgeSnapshotApplier;
    }

    public EdgeSyncBatchResponse execute(String deviceToken, EdgeSyncBatchRequest request) {
        if (request == null || request.storeId() == null) {
            throw new IllegalArgumentException("storeId é obrigatório no lote de sincronização.");
        }

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new StoreNotFoundException(request.storeId()));

        validateDeviceToken(deviceToken, store);

        List<UUID> ackedEventIds = new ArrayList<>();
        int processedCount = 0;

        List<EdgeSyncEventDto> events = request.events() != null ? request.events() : List.of();
        for (EdgeSyncEventDto event : events) {
            if (event.eventId() == null) {
                continue;
            }
            if (edgeOutboxRepository.isEventAlreadyProcessed(event.eventId())) {
                ackedEventIds.add(event.eventId());
                continue;
            }

            edgeSnapshotApplier.apply(
                    request.storeId(),
                    event.eventType(),
                    event.aggregateId(),
                    event.payloadJson()
            );
            edgeOutboxRepository.recordProcessedEvent(
                    event.eventId(),
                    request.storeId(),
                    event.eventType(),
                    event.aggregateId()
            );

            ackedEventIds.add(event.eventId());
            processedCount++;
        }

        Instant now = Instant.now();
        store.markEdgeSynced(now);
        storeRepository.save(store);

        return new EdgeSyncBatchResponse(processedCount, ackedEventIds, now);
    }

    private void validateDeviceToken(String deviceToken, Store store) {
        String configuredHash = store.getEdgeApiKeyHash();
        if (deviceToken == null || deviceToken.isBlank() || configuredHash == null || configuredHash.isBlank()) {
            throw new InvalidEdgeDeviceTokenException("Token de dispositivo Edge ausente ou não configurado para esta loja.");
        }

        boolean valid;
        if (configuredHash.startsWith("$2a$") || configuredHash.startsWith("$2b$") || configuredHash.startsWith("$2y$")) {
            valid = passwordEncoder.matches(deviceToken, configuredHash);
        } else {
            valid = MessageDigest.isEqual(
                    deviceToken.getBytes(StandardCharsets.UTF_8),
                    configuredHash.getBytes(StandardCharsets.UTF_8)
            );
        }

        if (!valid) {
            throw new InvalidEdgeDeviceTokenException("Token de dispositivo Edge inválido.");
        }
    }
}

package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationItemFingerprint;
import com.trinusphere.beebite.application.sync.port.EdgeReconciliationDigestPort;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class CompareEdgeReconciliationItemsUseCase {

    private final StoreRepository storeRepository;
    private final EdgeDeviceTokenValidator tokenValidator;
    private final EdgeReconciliationDigestPort digestPort;

    @Inject
    public CompareEdgeReconciliationItemsUseCase(
            StoreRepository storeRepository,
            EdgeDeviceTokenValidator tokenValidator,
            EdgeReconciliationDigestPort digestPort) {
        this.storeRepository = storeRepository;
        this.tokenValidator = tokenValidator;
        this.digestPort = digestPort;
    }

    public EdgeReconcileItemsResponse execute(String deviceToken, EdgeReconcileItemsRequest request) {
        if (request == null || request.storeId() == null
                || request.entityType() == null || request.bucketDate() == null) {
            throw new InvalidSyncRequestException(
                    "storeId, entityType e bucketDate são obrigatórios na reconciliação de itens.");
        }

        ReconciliationEntityType entityType;
        try {
            entityType = ReconciliationEntityType.valueOf(request.entityType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidSyncRequestException("entityType inválido para reconciliação: " + request.entityType());
        }

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new StoreNotFoundException(request.storeId()));
        tokenValidator.validate(deviceToken, store);

        List<ReconciliationItemFingerprint> edgeItems = request.items() != null ? request.items() : List.of();
        List<ReconciliationItemFingerprint> cloudItems = digestPort.computeItemFingerprints(
                request.storeId(), entityType, request.bucketDate());

        Map<UUID, String> edgeMap = new LinkedHashMap<>();
        for (ReconciliationItemFingerprint item : edgeItems) {
            if (item != null && item.id() != null) {
                edgeMap.put(item.id(), item.fingerprint());
            }
        }

        Map<UUID, String> cloudMap = new LinkedHashMap<>();
        for (ReconciliationItemFingerprint item : cloudItems) {
            if (item != null && item.id() != null) {
                cloudMap.put(item.id(), item.fingerprint());
            }
        }

        List<UUID> missingInCloud = new ArrayList<>();
        List<UUID> differentInCloud = new ArrayList<>();
        List<UUID> extraInCloud = new ArrayList<>();

        for (Map.Entry<UUID, String> entry : edgeMap.entrySet()) {
            UUID id = entry.getKey();
            if (!cloudMap.containsKey(id)) {
                missingInCloud.add(id);
            } else if (!Objects.equals(entry.getValue(), cloudMap.get(id))) {
                differentInCloud.add(id);
            }
        }

        for (UUID cloudId : cloudMap.keySet()) {
            if (!edgeMap.containsKey(cloudId)) {
                extraInCloud.add(cloudId);
            }
        }

        return new EdgeReconcileItemsResponse(missingInCloud, differentInCloud, extraInCloud);
    }
}

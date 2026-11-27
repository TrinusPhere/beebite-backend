package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationBucketDigest;
import com.trinusphere.beebite.application.sync.port.EdgeReconciliationDigestPort;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
public class CompareEdgeReconciliationBucketsUseCase {

    private static final long MAX_WINDOW_DAYS = 31L;

    private final StoreRepository storeRepository;
    private final EdgeDeviceTokenValidator tokenValidator;
    private final EdgeReconciliationDigestPort digestPort;

    @Inject
    public CompareEdgeReconciliationBucketsUseCase(
            StoreRepository storeRepository,
            EdgeDeviceTokenValidator tokenValidator,
            EdgeReconciliationDigestPort digestPort) {
        this.storeRepository = storeRepository;
        this.tokenValidator = tokenValidator;
        this.digestPort = digestPort;
    }

    public EdgeReconcileBucketsResponse execute(String deviceToken, EdgeReconcileBucketsRequest request) {
        if (request == null || request.storeId() == null || request.from() == null || request.to() == null) {
            throw new InvalidSyncRequestException("storeId, from e to são obrigatórios para reconciliação.");
        }
        if (request.from().isAfter(request.to())) {
            throw new InvalidSyncRequestException("Data inicial não pode ser posterior à data final.");
        }
        long days = ChronoUnit.DAYS.between(request.from(), request.to()) + 1L;
        if (days > MAX_WINDOW_DAYS) {
            throw new InvalidSyncRequestException("A janela de reconciliação não pode exceder 31 dias.");
        }

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new StoreNotFoundException(request.storeId()));
        tokenValidator.validate(deviceToken, store);

        List<ReconciliationBucketDigest> edgeList = request.digests() != null ? request.digests() : List.of();
        List<ReconciliationBucketDigest> cloudList = digestPort.computeBucketDigests(
                request.storeId(), request.from(), request.to());

        Map<BucketKey, ReconciliationBucketDigest> edgeMap = new LinkedHashMap<>();
        for (ReconciliationBucketDigest d : edgeList) {
            if (d != null && d.entityType() != null && d.bucketDate() != null) {
                edgeMap.put(new BucketKey(d.entityType().toUpperCase(), d.bucketDate()), d);
            }
        }

        Map<BucketKey, ReconciliationBucketDigest> cloudMap = new LinkedHashMap<>();
        for (ReconciliationBucketDigest d : cloudList) {
            if (d != null && d.entityType() != null && d.bucketDate() != null) {
                cloudMap.put(new BucketKey(d.entityType().toUpperCase(), d.bucketDate()), d);
            }
        }

        Set<BucketKey> allKeys = new LinkedHashSet<>(edgeMap.keySet());
        allKeys.addAll(cloudMap.keySet());

        List<ReconciliationBucketDigest> mismatched = new ArrayList<>();
        for (BucketKey key : allKeys) {
            ReconciliationBucketDigest e = edgeMap.get(key);
            ReconciliationBucketDigest c = cloudMap.get(key);
            long edgeCount = e != null ? e.count() : 0L;
            long cloudCount = c != null ? c.count() : 0L;
            String edgeHash = e != null && e.hash() != null ? e.hash() : "";
            String cloudHash = c != null && c.hash() != null ? c.hash() : "";

            if (edgeCount != cloudCount || !Objects.equals(edgeHash, cloudHash)) {
                mismatched.add(new ReconciliationBucketDigest(key.entityType(), key.bucketDate(), cloudCount, cloudHash));
            }
        }

        return new EdgeReconcileBucketsResponse(mismatched, Instant.now());
    }

    private record BucketKey(String entityType, LocalDate bucketDate) {
    }
}


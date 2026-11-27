package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationBucketDigest;
import com.trinusphere.beebite.application.sync.dto.ReconciliationItemFingerprint;
import com.trinusphere.beebite.application.sync.dto.ReconciliationResult;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.application.sync.port.EdgeOutboxRequeuePort;
import com.trinusphere.beebite.application.sync.port.EdgeReconciliationDigestPort;
import com.trinusphere.beebite.domain.sync.entity.EdgeReconciliationRun;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeReconciliationRunRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class ReconcileEdgeDataUseCase {

    private static final Logger LOG = Logger.getLogger(ReconcileEdgeDataUseCase.class.getName());

    private final EdgeOutboxRepository outboxRepository;
    private final EdgeSyncStreamStateRepository streamStateRepository;
    private final EdgeReconciliationRunRepository runRepository;
    private final EdgeReconciliationDigestPort digestPort;
    private final EdgeOutboxRequeuePort requeuePort;
    private final EdgeCloudSyncClient cloudClient;
    private final String syncMode;
    private final String edgeStoreId;
    private final String deviceToken;

    @Inject
    public ReconcileEdgeDataUseCase(
            EdgeOutboxRepository outboxRepository,
            EdgeSyncStreamStateRepository streamStateRepository,
            EdgeReconciliationRunRepository runRepository,
            EdgeReconciliationDigestPort digestPort,
            EdgeOutboxRequeuePort requeuePort,
            EdgeCloudSyncClient cloudClient,
            @ConfigProperty(name = "biteflow.sync.mode", defaultValue = "CLOUD") String syncMode,
            @ConfigProperty(name = "biteflow.edge.store-id") Optional<String> edgeStoreId,
            @ConfigProperty(name = "biteflow.sync.edge-device-token", defaultValue = "") String deviceToken) {
        this.outboxRepository = outboxRepository;
        this.streamStateRepository = streamStateRepository;
        this.runRepository = runRepository;
        this.digestPort = digestPort;
        this.requeuePort = requeuePort;
        this.cloudClient = cloudClient;
        this.syncMode = syncMode != null ? syncMode.trim().toUpperCase() : "CLOUD";
        this.edgeStoreId = edgeStoreId != null ? edgeStoreId.orElse("").trim() : "";
        this.deviceToken = deviceToken != null ? deviceToken.trim() : "";
    }

    public Optional<ReconciliationResult> execute(
            ReconciliationTrigger trigger,
            LocalDate from,
            LocalDate to) {
        if (!"EDGE".equals(syncMode) || edgeStoreId.isBlank()) {
            return Optional.empty();
        }

        UUID storeId;
        try {
            storeId = UUID.fromString(edgeStoreId);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        EdgeSyncStreamState state = streamStateRepository.findOrInitialize(storeId);
        if (!state.canAttempt(now) || outboxRepository.findStoreIdsWithPending().contains(storeId)) {
            return Optional.empty();
        }

        EdgeReconciliationRun run = runRepository.save(EdgeReconciliationRun.start(storeId, trigger, from, to));
        try {
            List<ReconciliationBucketDigest> localDigests = digestPort.computeBucketDigests(storeId, from, to);
            EdgeReconcileBucketsResponse bucketResp = cloudClient.compareBuckets(
                    new EdgeReconcileBucketsRequest(storeId, from, to, localDigests),
                    deviceToken
            );

            List<ReconciliationBucketDigest> mismatched = (bucketResp != null && bucketResp.mismatched() != null)
                    ? bucketResp.mismatched()
                    : List.of();
            run.recordComparison(localDigests.size(), mismatched.size());

            int totalRequeued = 0;
            int totalExtraInCloud = 0;

            for (ReconciliationBucketDigest bucket : mismatched) {
                ReconciliationEntityType type;
                try {
                    type = ReconciliationEntityType.valueOf(bucket.entityType().trim().toUpperCase());
                } catch (Exception ignored) {
                    continue;
                }

                List<ReconciliationItemFingerprint> localItems = digestPort.computeItemFingerprints(
                        storeId, type, bucket.bucketDate());
                EdgeReconcileItemsResponse itemDiff = cloudClient.compareItems(
                        new EdgeReconcileItemsRequest(storeId, type.name(), bucket.bucketDate(), localItems),
                        deviceToken
                );
                if (itemDiff == null) {
                    continue;
                }

                Set<UUID> toRequeue = new LinkedHashSet<>();
                if (itemDiff.missingInCloud() != null) {
                    toRequeue.addAll(itemDiff.missingInCloud());
                }
                if (itemDiff.differentInCloud() != null) {
                    toRequeue.addAll(itemDiff.differentInCloud());
                }
                if (!toRequeue.isEmpty()) {
                    totalRequeued += requeuePort.requeue(storeId, type, new ArrayList<>(toRequeue));
                }

                if (itemDiff.extraInCloud() != null && !itemDiff.extraInCloud().isEmpty()) {
                    totalExtraInCloud += itemDiff.extraInCloud().size();
                    LOG.warning(() -> String.format(
                            "Reconciliação %s (%s): %d registro(s) presentes apenas na nuvem: %s",
                            type.name(), bucket.bucketDate(), itemDiff.extraInCloud().size(), itemDiff.extraInCloud()));
                }
            }

            run.recordRepair(totalRequeued, totalExtraInCloud);
            run.finish(Instant.now());
            run = runRepository.save(run);

            if (trigger == ReconciliationTrigger.FULL_RESYNC) {
                state.clearFullResync();
                streamStateRepository.save(state);
            }

            return Optional.of(EdgeSyncDtoMapper.toReconciliationResult(run));
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            run.fail(msg, Instant.now());
            run = runRepository.save(run);
            return Optional.of(EdgeSyncDtoMapper.toReconciliationResult(run));
        }
    }
}


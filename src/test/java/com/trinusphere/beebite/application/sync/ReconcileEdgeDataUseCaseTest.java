package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconcileEdgeDataUseCaseTest {

    @Mock
    EdgeOutboxRepository outboxRepository;
    @Mock
    EdgeSyncStreamStateRepository streamStateRepository;
    @Mock
    EdgeReconciliationRunRepository runRepository;
    @Mock
    EdgeReconciliationDigestPort digestPort;
    @Mock
    EdgeOutboxRequeuePort requeuePort;
    @Mock
    EdgeCloudSyncClient cloudClient;

    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private ReconcileEdgeDataUseCase reconcileUseCase;
    private RunRequestedFullResyncUseCase fullResyncUseCase;

    @BeforeEach
    void setUp() {
        reconcileUseCase = new ReconcileEdgeDataUseCase(
                outboxRepository,
                streamStateRepository,
                runRepository,
                digestPort,
                requeuePort,
                cloudClient,
                "EDGE",
                Optional.of(STORE_ID.toString()),
                "edge-token"
        );
        fullResyncUseCase = new RunRequestedFullResyncUseCase(
                streamStateRepository,
                reconcileUseCase,
                "EDGE",
                Optional.of(STORE_ID.toString())
        );
    }

    @Test
    void shouldSkipReconciliationWhenPendingEventsExist() {
        when(streamStateRepository.findOrInitialize(STORE_ID)).thenReturn(EdgeSyncStreamState.initial(STORE_ID));
        when(outboxRepository.findStoreIdsWithPending()).thenReturn(List.of(STORE_ID));

        Optional<ReconciliationResult> res = reconcileUseCase.execute(
                ReconciliationTrigger.SCHEDULED, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 26));

        assertTrue(res.isEmpty());
        verify(cloudClient, never()).compareBuckets(any(), any());
    }

    @Test
    void shouldRepairMissingAndDifferentItemsAndClearFullResyncFlag() {
        EdgeSyncStreamState state = EdgeSyncStreamState.initial(STORE_ID);
        state.requestFullResync();
        when(streamStateRepository.findOrInitialize(STORE_ID)).thenReturn(state);
        when(outboxRepository.findStoreIdsWithPending()).thenReturn(List.of());
        when(runRepository.save(any(EdgeReconciliationRun.class))).thenAnswer(inv -> {
            EdgeReconciliationRun r = inv.getArgument(0);
            if (r.getId() == null) {
                r.setId(UUID.randomUUID());
            }
            return r;
        });

        LocalDate date = LocalDate.of(2026, 11, 26);
        UUID missingId = UUID.randomUUID();
        UUID diffId = UUID.randomUUID();
        UUID extraId = UUID.randomUUID();

        when(digestPort.computeBucketDigests(eq(STORE_ID), any(), any()))
                .thenReturn(List.of(new ReconciliationBucketDigest("ORDER", date, 2, "hash-local")));
        when(cloudClient.compareBuckets(any(), eq("edge-token")))
                .thenReturn(new EdgeReconcileBucketsResponse(
                        List.of(new ReconciliationBucketDigest("ORDER", date, 1, "hash-cloud")), Instant.now()));
        when(digestPort.computeItemFingerprints(STORE_ID, ReconciliationEntityType.ORDER, date))
                .thenReturn(List.of(
                        new ReconciliationItemFingerprint(missingId, "fp1"),
                        new ReconciliationItemFingerprint(diffId, "fp2")
                ));
        when(cloudClient.compareItems(any(), eq("edge-token")))
                .thenReturn(new EdgeReconcileItemsResponse(List.of(missingId), List.of(diffId), List.of(extraId)));
        when(requeuePort.requeue(eq(STORE_ID), eq(ReconciliationEntityType.ORDER), eq(List.of(missingId, diffId))))
                .thenReturn(2);

        boolean ran = fullResyncUseCase.execute();

        assertTrue(ran);
        assertFalse(state.isFullResyncRequested());
        verify(requeuePort).requeue(STORE_ID, ReconciliationEntityType.ORDER, List.of(missingId, diffId));
    }
}


package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.EdgeDeviceTokenValidator;
import com.trinusphere.beebite.application.sync.EdgeSyncAckHandler;
import com.trinusphere.beebite.application.sync.FlushEdgeOutboxUseCase;
import com.trinusphere.beebite.application.sync.IngestEdgeSyncBatchUseCase;
import com.trinusphere.beebite.application.sync.ProbeEdgeSyncCursorUseCase;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EdgeFalseSyncedRecoveryIT {

    @Inject
    StoreRepository storeRepository;
    @Inject
    EdgeOutboxRepository outboxRepository;
    @Inject
    EdgeSyncCursorRepository cursorRepository;
    @Inject
    EdgeSyncStreamStateRepository streamStateRepository;
    @Inject
    IngestEdgeSyncBatchUseCase ingestUseCase;
    @Inject
    EdgeDeviceTokenValidator tokenValidator;

    @Test
    void shouldDetectFalseSyncedEventsWhenCloudLosesDataAndRecoverAutomatically() {
        Store store = new Store("Balcão Teste Recovery");
        store.configureEdgeSync("EDGE", "recovery-secret");
        store = storeRepository.save(store);
        UUID storeId = store.getId();

        for (int i = 1; i <= 5; i++) {
            UUID regId = UUID.randomUUID();
            outboxRepository.save(new EdgeOutboxEvent(
                    storeId, "CASH_REGISTER_UPSERT", regId, "{\"name\":\"Caixa " + i + "\",\"active\":true}"));
        }

        EdgeCloudSyncClient inMemoryLoopbackClient = new EdgeCloudSyncClient() {
            @Override
            public EdgeSyncBatchResponse sendBatch(EdgeSyncBatchRequest batchRequest, String deviceToken) {
                return ingestUseCase.execute(deviceToken, batchRequest);
            }

            @Override
            public EdgeCatalogDeltaResponse pullCatalogDelta(UUID sId, Instant since, String token) {
                throw new UnsupportedOperationException();
            }

            @Override
            public EdgeReconcileBucketsResponse compareBuckets(EdgeReconcileBucketsRequest req, String token) {
                throw new UnsupportedOperationException();
            }

            @Override
            public EdgeReconcileItemsResponse compareItems(EdgeReconcileItemsRequest req, String token) {
                throw new UnsupportedOperationException();
            }
        };

        EdgeSyncAckHandler ackHandler = new EdgeSyncAckHandler(outboxRepository, streamStateRepository);
        FlushEdgeOutboxUseCase flush = new FlushEdgeOutboxUseCase(
                outboxRepository, streamStateRepository, inMemoryLoopbackClient, ackHandler,
                "EDGE", "recovery-secret", Duration.ofSeconds(15));
        ProbeEdgeSyncCursorUseCase probe = new ProbeEdgeSyncCursorUseCase(
                outboxRepository, streamStateRepository, inMemoryLoopbackClient, ackHandler,
                "EDGE", Optional.of(storeId.toString()), "recovery-secret", Duration.ofSeconds(15));

        assertEquals(5, flush.execute());
        assertEquals(0L, outboxRepository.countByStatus(storeId, EdgeOutboxStatus.PENDING));

        // Simula perda na nuvem (ex.: restauração de backup antigo em seq=2)
        EdgeSyncCursor regressed = cursorRepository.findByStoreId(storeId).orElseThrow();
        regressed.rebase(2L);
        cursorRepository.save(regressed);

        assertTrue(probe.execute());
        assertEquals(3L, outboxRepository.countByStatus(storeId, EdgeOutboxStatus.PENDING));

        assertEquals(3, flush.execute());
        assertEquals(0L, outboxRepository.countByStatus(storeId, EdgeOutboxStatus.PENDING));
        assertEquals(5L, cursorRepository.findByStoreId(storeId).orElseThrow().getLastContiguousSequence());
    }
}


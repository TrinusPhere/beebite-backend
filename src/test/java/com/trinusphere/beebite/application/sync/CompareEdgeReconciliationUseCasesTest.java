package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileBucketsResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeReconcileItemsResponse;
import com.trinusphere.beebite.application.sync.dto.ReconciliationBucketDigest;
import com.trinusphere.beebite.application.sync.dto.ReconciliationItemFingerprint;
import com.trinusphere.beebite.application.sync.port.EdgeReconciliationDigestPort;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareEdgeReconciliationUseCasesTest {

    @Mock
    StoreRepository storeRepository;
    @Mock
    EdgeReconciliationDigestPort digestPort;
    @Mock
    PasswordEncoder passwordEncoder;

    private static final UUID STORE_ID = UUID.randomUUID();
    private Store store;
    private CompareEdgeReconciliationBucketsUseCase compareBucketsUseCase;
    private CompareEdgeReconciliationItemsUseCase compareItemsUseCase;

    @BeforeEach
    void setUp() {
        store = Store.reconstitute(STORE_ID, "Balcão", null, true, null, null, "balcao", "EDGE", "tok", null);
        EdgeDeviceTokenValidator validator = new EdgeDeviceTokenValidator(passwordEncoder);
        compareBucketsUseCase = new CompareEdgeReconciliationBucketsUseCase(storeRepository, validator, digestPort);
        compareItemsUseCase = new CompareEdgeReconciliationItemsUseCase(storeRepository, validator, digestPort);
    }

    @Test
    void shouldIdentifyMismatchedMissingAndCloudOnlyBuckets() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        LocalDate d1 = LocalDate.of(2026, 11, 25);
        LocalDate d2 = LocalDate.of(2026, 11, 26);

        List<ReconciliationBucketDigest> edgeDigests = List.of(
                new ReconciliationBucketDigest("ORDER", d1, 10, "aaa"),
                new ReconciliationBucketDigest("ORDER", d2, 5, "bbb")
        );
        List<ReconciliationBucketDigest> cloudDigests = List.of(
                new ReconciliationBucketDigest("ORDER", d1, 10, "aaa"),
                new ReconciliationBucketDigest("PAYMENT", d2, 2, "ccc")
        );
        when(digestPort.computeBucketDigests(STORE_ID, d1, d2)).thenReturn(cloudDigests);

        EdgeReconcileBucketsResponse resp = compareBucketsUseCase.execute(
                "tok", new EdgeReconcileBucketsRequest(STORE_ID, d1, d2, edgeDigests));

        assertEquals(2, resp.mismatched().size());
        assertTrue(resp.mismatched().stream().anyMatch(b -> b.entityType().equals("ORDER") && b.bucketDate().equals(d2) && b.count() == 0));
        assertTrue(resp.mismatched().stream().anyMatch(b -> b.entityType().equals("PAYMENT") && b.count() == 2));
    }

    @Test
    void shouldRejectWindowLargerThan31Days() {
        assertThrows(InvalidSyncRequestException.class, () -> compareBucketsUseCase.execute(
                "tok",
                new EdgeReconcileBucketsRequest(
                        STORE_ID,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 11, 26),
                        List.of()
                )
        ));
    }

    @Test
    void shouldClassifyMissingDifferentAndExtraItemsInCloud() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        LocalDate date = LocalDate.of(2026, 11, 26);
        UUID sameId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        UUID diffId = UUID.randomUUID();
        UUID extraId = UUID.randomUUID();

        when(digestPort.computeItemFingerprints(STORE_ID, ReconciliationEntityType.ORDER, date)).thenReturn(List.of(
                new ReconciliationItemFingerprint(sameId, "fp1"),
                new ReconciliationItemFingerprint(diffId, "fp-old"),
                new ReconciliationItemFingerprint(extraId, "fp-extra")
        ));

        EdgeReconcileItemsResponse resp = compareItemsUseCase.execute("tok", new EdgeReconcileItemsRequest(
                STORE_ID, "ORDER", date, List.of(
                        new ReconciliationItemFingerprint(sameId, "fp1"),
                        new ReconciliationItemFingerprint(missingId, "fp2"),
                        new ReconciliationItemFingerprint(diffId, "fp-new")
                )
        ));

        assertEquals(List.of(missingId), resp.missingInCloud());
        assertEquals(List.of(diffId), resp.differentInCloud());
        assertEquals(List.of(extraId), resp.extraInCloud());
    }
}


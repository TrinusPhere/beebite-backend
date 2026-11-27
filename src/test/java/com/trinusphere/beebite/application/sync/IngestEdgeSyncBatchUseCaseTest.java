package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeEventInbox;
import com.trinusphere.beebite.application.sync.port.EdgeEventOutcome;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestEdgeSyncBatchUseCaseTest {

    @Mock
    StoreRepository storeRepository;
    @Mock
    EdgeEventInbox edgeEventInbox;
    @Mock
    EdgeSyncCursorRepository cursorRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private Store store;
    private IngestEdgeSyncBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        store = Store.reconstitute(
                STORE_ID, "Balcão", null, true, null, null, "balcao",
                "EDGE", "valid-token", null
        );
        EdgeDeviceTokenValidator validator = new EdgeDeviceTokenValidator(passwordEncoder);
        useCase = new IngestEdgeSyncBatchUseCase(storeRepository, validator, edgeEventInbox, cursorRepository);
    }

    @Test
    void shouldSeparateAckedAndRejectedInLegacyBatchAndHandlePartialTransientError() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        EdgeSyncEventDto ev1 = new EdgeSyncEventDto(id1, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), null);
        EdgeSyncEventDto ev2 = new EdgeSyncEventDto(id2, "FOO_UPSERT", UUID.randomUUID(), "{}", Instant.now(), null);
        EdgeSyncEventDto ev3 = new EdgeSyncEventDto(id3, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), null);

        when(edgeEventInbox.applyLegacy(STORE_ID, ev1)).thenReturn(EdgeEventOutcome.applied(id1));
        when(edgeEventInbox.applyLegacy(STORE_ID, ev2))
                .thenReturn(EdgeEventOutcome.rejected(id2, "Tipo não suportado"));
        when(edgeEventInbox.applyLegacy(STORE_ID, ev3)).thenThrow(new RuntimeException("DB lock timeout"));

        EdgeSyncBatchResponse resp = useCase.execute("valid-token", new EdgeSyncBatchRequest(STORE_ID, List.of(ev1, ev2, ev3), null));

        assertEquals(1, resp.processedCount());
        assertEquals(List.of(id1), resp.ackedEventIds());
        assertEquals(1, resp.rejectedEvents().size());
        assertEquals(id2, resp.rejectedEvents().get(0).eventId());
    }

    @Test
    void shouldRethrowWhenFirstEventFailsTransiently() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        UUID id1 = UUID.randomUUID();
        EdgeSyncEventDto ev1 = new EdgeSyncEventDto(id1, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), null);
        when(edgeEventInbox.applyLegacy(STORE_ID, ev1)).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class,
                () -> useCase.execute("valid-token", new EdgeSyncBatchRequest(STORE_ID, List.of(ev1), null)));
    }

    @Test
    void shouldProcessSequencedEventsDeduplicateAndStopAtSequenceGap() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(cursorRepository.findByStoreId(STORE_ID))
                .thenReturn(Optional.of(EdgeSyncCursor.reconstitute(STORE_ID, 5L)));

        UUID dupId = UUID.randomUUID();
        UUID nextId = UUID.randomUUID();
        UUID gapId = UUID.randomUUID();
        EdgeSyncEventDto ev5 = new EdgeSyncEventDto(dupId, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), 5L);
        EdgeSyncEventDto ev6 = new EdgeSyncEventDto(nextId, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), 6L);
        EdgeSyncEventDto ev8 = new EdgeSyncEventDto(gapId, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), 8L);

        when(edgeEventInbox.applyInOrder(STORE_ID, ev6)).thenReturn(EdgeEventOutcome.applied(nextId));

        EdgeSyncBatchResponse resp = useCase.execute(
                "valid-token",
                new EdgeSyncBatchRequest(STORE_ID, List.of(ev5, ev6, ev8), 1L)
        );

        assertEquals(1, resp.processedCount());
        assertEquals(List.of(dupId, nextId), resp.ackedEventIds());
        assertEquals(6L, resp.lastContiguousSequence());
        assertFalse(resp.requiresFullResync());
        verify(edgeEventInbox, never()).applyInOrder(eq(STORE_ID), eq(ev5));
        verify(edgeEventInbox, never()).applyInOrder(eq(STORE_ID), eq(ev8));
    }

    @Test
    void shouldDetectUnrecoverableGapRebaseCursorAndRequestFullResync() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(cursorRepository.findByStoreId(STORE_ID))
                .thenReturn(Optional.of(EdgeSyncCursor.reconstitute(STORE_ID, 10L)));
        when(cursorRepository.save(any(EdgeSyncCursor.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID evId = UUID.randomUUID();
        EdgeSyncEventDto ev50 = new EdgeSyncEventDto(evId, "ORDER_UPSERT", UUID.randomUUID(), "{}", Instant.now(), 50L);
        when(edgeEventInbox.applyInOrder(STORE_ID, ev50)).thenReturn(EdgeEventOutcome.applied(evId));

        EdgeSyncBatchResponse resp = useCase.execute(
                "valid-token",
                new EdgeSyncBatchRequest(STORE_ID, List.of(ev50), 50L)
        );

        assertTrue(resp.requiresFullResync());
        assertEquals(50L, resp.lastContiguousSequence());
        assertEquals(List.of(evId), resp.ackedEventIds());
    }

    @Test
    void shouldReturnCurrentCursorOnEmptyProbeRequest() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(cursorRepository.findByStoreId(STORE_ID))
                .thenReturn(Optional.of(EdgeSyncCursor.reconstitute(STORE_ID, 12L)));

        EdgeSyncBatchResponse resp = useCase.execute(
                "valid-token",
                new EdgeSyncBatchRequest(STORE_ID, List.of(), 1L)
        );

        assertEquals(0, resp.processedCount());
        assertEquals(12L, resp.lastContiguousSequence());
        assertFalse(resp.requiresFullResync());
    }
}


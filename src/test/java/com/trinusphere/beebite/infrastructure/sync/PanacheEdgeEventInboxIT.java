package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeEventInbox;
import com.trinusphere.beebite.application.sync.port.EdgeEventOutcome;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class PanacheEdgeEventInboxIT {

    private static final UUID DEFAULT_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    EdgeEventInbox inbox;

    @Inject
    EdgeSyncCursorRepository cursorRepository;

    @Test
    void shouldRejectUnknownEventLogRejectionAndDeduplicateValidLegacyEvent() {
        UUID rejectedId = UUID.randomUUID();
        EdgeSyncEventDto bad = new EdgeSyncEventDto(
                rejectedId, "FOO_UNKNOWN_UPSERT", UUID.randomUUID(), "{}", Instant.now(), null);
        EdgeEventOutcome out1 = inbox.applyLegacy(DEFAULT_STORE_ID, bad);
        assertEquals(EdgeEventOutcome.Status.REJECTED, out1.status());

        UUID validId = UUID.randomUUID();
        EdgeSyncEventDto ok = new EdgeSyncEventDto(
                validId, "STORE_STATUS_UPSERT", DEFAULT_STORE_ID, "{\"open\":true}", Instant.now(), null);
        assertEquals(EdgeEventOutcome.Status.APPLIED, inbox.applyLegacy(DEFAULT_STORE_ID, ok).status());
        assertEquals(EdgeEventOutcome.Status.DUPLICATE, inbox.applyLegacy(DEFAULT_STORE_ID, ok).status());

        EdgeSyncCursor cursor = cursorRepository.findByStoreId(DEFAULT_STORE_ID)
                .orElseGet(() -> cursorRepository.save(EdgeSyncCursor.initialize(DEFAULT_STORE_ID, 0L)));
        long nextSeq = cursor.getLastContiguousSequence() + 1L;
        EdgeSyncEventDto seqRejected = new EdgeSyncEventDto(
                UUID.randomUUID(), "FOO_UNKNOWN_UPSERT", UUID.randomUUID(), "{}", Instant.now(), nextSeq);
        assertEquals(EdgeEventOutcome.Status.REJECTED, inbox.applyInOrder(DEFAULT_STORE_ID, seqRejected).status());
        assertEquals(nextSeq, cursorRepository.findByStoreId(DEFAULT_STORE_ID).orElseThrow().getLastContiguousSequence());
    }
}


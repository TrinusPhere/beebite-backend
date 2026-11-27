package com.trinusphere.beebite.domain.sync;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EdgeSyncCursorTest {

    @Test
    void shouldInitializeAndAdvanceSequentially() {
        UUID storeId = UUID.randomUUID();
        EdgeSyncCursor cursor = EdgeSyncCursor.initialize(storeId, 0L);

        assertEquals(storeId, cursor.getStoreId());
        assertEquals(0L, cursor.getLastContiguousSequence());
        assertTrue(cursor.isNext(1L));
        assertFalse(cursor.isDuplicate(1L));

        cursor.advanceTo(1L);
        assertEquals(1L, cursor.getLastContiguousSequence());
        assertTrue(cursor.isDuplicate(1L));
        assertTrue(cursor.isNext(2L));
    }

    @Test
    void shouldRejectOutOfOrderAdvance() {
        EdgeSyncCursor cursor = EdgeSyncCursor.initialize(UUID.randomUUID(), 5L);

        assertThrows(IllegalStateException.class, () -> cursor.advanceTo(7L));
        assertThrows(IllegalStateException.class, () -> cursor.advanceTo(5L));
    }

    @Test
    void shouldDetectUnrecoverableGapAndRebase() {
        EdgeSyncCursor cursor = EdgeSyncCursor.reconstitute(UUID.randomUUID(), 10L);

        assertFalse(cursor.hasUnrecoverableGap(11L));
        assertFalse(cursor.hasUnrecoverableGap(5L));
        assertTrue(cursor.hasUnrecoverableGap(12L));

        cursor.rebase(11L);
        assertEquals(11L, cursor.getLastContiguousSequence());
        assertFalse(cursor.hasUnrecoverableGap(12L));
    }

    @Test
    void shouldClampNegativeBaselineToZero() {
        EdgeSyncCursor cursor = EdgeSyncCursor.initialize(UUID.randomUUID(), -5L);
        assertEquals(0L, cursor.getLastContiguousSequence());
    }
}


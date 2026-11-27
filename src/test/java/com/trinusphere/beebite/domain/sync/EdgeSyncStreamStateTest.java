package com.trinusphere.beebite.domain.sync;

import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EdgeSyncStreamStateTest {

    @Test
    void shouldApplyExponentialBackoffUpToFiveMinutesAndResetOnSuccess() {
        UUID storeId = UUID.randomUUID();
        EdgeSyncStreamState state = EdgeSyncStreamState.initial(storeId);
        Instant t0 = Instant.parse("2026-11-26T12:00:00Z");
        Duration pollInterval = Duration.ofSeconds(15);

        assertTrue(state.canAttempt(t0));

        // 1ª falha: 15s * 2^1 = 30s
        state.recordFailure("Connection refused", t0, pollInterval);
        assertEquals(1, state.getConsecutiveFailures());
        assertEquals(t0.plusSeconds(30), state.getNextAttemptAt());
        assertFalse(state.canAttempt(t0.plusSeconds(29)));
        assertTrue(state.canAttempt(t0.plusSeconds(30)));

        // 2ª falha: 15s * 2^2 = 60s
        state.recordFailure("Connection refused", t0, pollInterval);
        assertEquals(2, state.getConsecutiveFailures());
        assertEquals(t0.plusSeconds(60), state.getNextAttemptAt());

        // Muitas falhas: teto de 5 minutos (300s)
        for (int i = 0; i < 8; i++) {
            state.recordFailure("Still down", t0, pollInterval);
        }
        assertEquals(t0.plusSeconds(300), state.getNextAttemptAt());

        // Sucesso zera falhas e registra cursor confirmado
        Instant tSuccess = t0.plusSeconds(310);
        state.recordSuccess(128L, tSuccess);
        assertEquals(0, state.getConsecutiveFailures());
        assertNull(state.getNextAttemptAt());
        assertNull(state.getLastError());
        assertEquals(128L, state.getCloudAckSequence());
        assertEquals(tSuccess, state.getLastSuccessAt());
        assertTrue(state.canAttempt(tSuccess));
    }

    @Test
    void shouldManageFullResyncFlag() {
        EdgeSyncStreamState state = EdgeSyncStreamState.initial(UUID.randomUUID());
        assertFalse(state.isFullResyncRequested());

        state.requestFullResync();
        assertTrue(state.isFullResyncRequested());

        state.clearFullResync();
        assertFalse(state.isFullResyncRequested());
    }
}


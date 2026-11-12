package com.fidecode.beebite.domain.sync;

import com.fidecode.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.fidecode.beebite.domain.sync.valueobject.EdgeOutboxStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EdgeOutboxEventTest {

    @Test
    void shouldCreatePendingOutboxEventByDefault() {
        UUID storeId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        String payload = "{\"displayId\":101,\"totalAmount\":45.90}";

        EdgeOutboxEvent event = new EdgeOutboxEvent(
                storeId,
                "ORDER_UPSERT",
                aggregateId,
                payload
        );

        assertEquals(storeId, event.getStoreId());
        assertEquals("ORDER_UPSERT", event.getEventType());
        assertEquals(aggregateId, event.getAggregateId());
        assertEquals(payload, event.getPayloadJson());
        assertEquals(EdgeOutboxStatus.PENDING, event.getStatus());
        assertEquals(0, event.getAttempts());
        assertNotNull(event.getCreatedAt());
        assertNull(event.getSyncedAt());
    }

    @Test
    void shouldMarkEventAsSynced() {
        EdgeOutboxEvent event = new EdgeOutboxEvent(
                UUID.randomUUID(),
                "CASH_SESSION_UPSERT",
                UUID.randomUUID(),
                "{\"status\":\"CLOSED\"}"
        );

        Instant now = Instant.now();
        event.markSynced(now);

        assertEquals(EdgeOutboxStatus.SYNCED, event.getStatus());
        assertEquals(now, event.getSyncedAt());
        assertNull(event.getLastError());
    }

    @Test
    void shouldRecordFailureAndIncrementAttempts() {
        EdgeOutboxEvent event = new EdgeOutboxEvent(
                UUID.randomUUID(),
                "STOCK_MOVEMENT_CREATED",
                UUID.randomUUID(),
                "{\"quantity\":2}"
        );

        event.recordAttemptFailure("Connection timeout");

        assertEquals(EdgeOutboxStatus.PENDING, event.getStatus());
        assertEquals(1, event.getAttempts());
        assertEquals("Connection timeout", event.getLastError());
    }

    @Test
    void shouldRejectInvalidConstructorArguments() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> new EdgeOutboxEvent(null, "ORDER_UPSERT", id, "{}"));
        assertThrows(IllegalArgumentException.class,
                () -> new EdgeOutboxEvent(id, " ", id, "{}"));
        assertThrows(IllegalArgumentException.class,
                () -> new EdgeOutboxEvent(id, "ORDER_UPSERT", null, "{}"));
        assertThrows(IllegalArgumentException.class,
                () -> new EdgeOutboxEvent(id, "ORDER_UPSERT", id, ""));
    }
}

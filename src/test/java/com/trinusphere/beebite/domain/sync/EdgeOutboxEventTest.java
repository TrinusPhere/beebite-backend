package com.trinusphere.beebite.domain.sync;

import com.trinusphere.beebite.domain.sync.entity.EdgeOutboxEvent;
import com.trinusphere.beebite.domain.sync.exception.EdgeEventRejectedException;
import com.trinusphere.beebite.domain.sync.exception.EdgeOutboxEventNotFoundException;
import com.trinusphere.beebite.domain.sync.exception.EdgeOutboxEventNotRetryableException;
import com.trinusphere.beebite.domain.sync.exception.EdgeSyncBusyException;
import com.trinusphere.beebite.domain.sync.exception.InvalidEdgeEventPayloadException;
import com.trinusphere.beebite.domain.sync.exception.UnsupportedEdgeEventTypeException;
import com.trinusphere.beebite.domain.sync.valueobject.EdgeOutboxStatus;
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
        assertNull(event.getSequence());
        assertNull(event.getRejectedReason());
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
    void shouldMarkEventAsRejectedAndClearSyncedAt() {
        EdgeOutboxEvent event = new EdgeOutboxEvent(
                UUID.randomUUID(),
                "FOO_UPSERT",
                UUID.randomUUID(),
                "{}"
        );
        event.markSynced(Instant.now());

        event.markRejected("Tipo de evento Edge não suportado: FOO_UPSERT");

        assertEquals(EdgeOutboxStatus.FAILED, event.getStatus());
        assertEquals("Tipo de evento Edge não suportado: FOO_UPSERT", event.getRejectedReason());
        assertNull(event.getSyncedAt());
    }

    @Test
    void shouldUseDefaultReasonWhenRejectedWithBlankMessage() {
        EdgeOutboxEvent event = new EdgeOutboxEvent(
                UUID.randomUUID(),
                "FOO_UPSERT",
                UUID.randomUUID(),
                "{}"
        );

        event.markRejected("   ");

        assertEquals(EdgeOutboxStatus.FAILED, event.getStatus());
        assertEquals("Rejeitado pela nuvem", event.getRejectedReason());
    }

    @Test
    void shouldReconstituteWithSequenceAndRejectedReason() {
        UUID id = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID aggId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-11-26T10:00:00Z");

        EdgeOutboxEvent event = EdgeOutboxEvent.reconstitute(
                id, storeId, "ORDER_UPSERT", aggId, "{\"id\":1}",
                EdgeOutboxStatus.FAILED, 2, "timeout", createdAt, null,
                42L, "Payload inválido"
        );

        assertEquals(42L, event.getSequence());
        assertEquals("Payload inválido", event.getRejectedReason());
        assertEquals(EdgeOutboxStatus.FAILED, event.getStatus());
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

    @Test
    void shouldMapDomainExceptionsToExpectedHttpStatuses() {
        EdgeEventRejectedException unsupported = new UnsupportedEdgeEventTypeException("FOO");
        EdgeEventRejectedException invalidPayload = new InvalidEdgeEventPayloadException("sem id");
        EdgeOutboxEventNotFoundException notFound = new EdgeOutboxEventNotFoundException(UUID.randomUUID());
        EdgeOutboxEventNotRetryableException notRetryable = new EdgeOutboxEventNotRetryableException(UUID.randomUUID());
        EdgeSyncBusyException busy = new EdgeSyncBusyException("Há eventos pendentes");

        assertEquals(422, unsupported.getHttpStatus());
        assertEquals(422, invalidPayload.getHttpStatus());
        assertEquals(404, notFound.getHttpStatus());
        assertEquals(422, notRetryable.getHttpStatus());
        assertEquals(409, busy.getHttpStatus());
    }
}

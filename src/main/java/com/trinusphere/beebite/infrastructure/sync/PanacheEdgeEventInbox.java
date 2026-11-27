package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeEventInbox;
import com.trinusphere.beebite.application.sync.port.EdgeEventOutcome;
import com.trinusphere.beebite.application.sync.port.EdgeSnapshotApplier;
import com.trinusphere.beebite.domain.shared.exception.ConcurrentModificationException;
import com.trinusphere.beebite.domain.sync.exception.EdgeEventRejectedException;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.EdgeSyncedEventLogEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.repository.EdgeSyncedEventLogPanacheRepo;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class PanacheEdgeEventInbox implements EdgeEventInbox {

    @Inject
    EdgeSnapshotApplier applier;

    @Inject
    EdgeSyncedEventLogPanacheRepo logRepo;

    @Inject
    EntityManager entityManager;

    @Override
    public EdgeEventOutcome applyLegacy(UUID storeId, EdgeSyncEventDto event) {
        try {
            return QuarkusTransaction.requiringNew().call(() -> {
                if (logRepo.findByIdOptional(event.eventId()).isPresent()) {
                    return EdgeEventOutcome.duplicate(event.eventId());
                }
                applier.apply(storeId, event.eventType(), event.aggregateId(), event.payloadJson());
                persistLog(event, storeId, null, "APPLIED", null);
                entityManager.flush();
                return EdgeEventOutcome.applied(event.eventId());
            });
        } catch (EdgeEventRejectedException ex) {
            recordRejectionLegacy(storeId, event, ex.getMessage());
            return EdgeEventOutcome.rejected(event.eventId(), ex.getMessage());
        } catch (PersistenceException ex) {
            throw new ConcurrentModificationException("Evento Edge", String.valueOf(event.eventId()));
        }
    }

    @Override
    public EdgeEventOutcome applyInOrder(UUID storeId, EdgeSyncEventDto event) {
        long sequence = event.sequence();
        try {
            return QuarkusTransaction.requiringNew().call(() -> {
                if (logRepo.findByIdOptional(event.eventId()).isPresent()) {
                    advanceCursor(storeId, sequence);
                    return EdgeEventOutcome.duplicate(event.eventId());
                }
                applier.apply(storeId, event.eventType(), event.aggregateId(), event.payloadJson());
                persistLog(event, storeId, sequence, "APPLIED", null);
                advanceCursor(storeId, sequence);
                entityManager.flush();
                return EdgeEventOutcome.applied(event.eventId());
            });
        } catch (EdgeEventRejectedException ex) {
            recordRejectionSequenced(storeId, event, sequence, ex.getMessage());
            return EdgeEventOutcome.rejected(event.eventId(), ex.getMessage());
        } catch (PersistenceException ex) {
            throw new ConcurrentModificationException("Evento Edge", String.valueOf(event.eventId()));
        }
    }

    private void recordRejectionLegacy(UUID storeId, EdgeSyncEventDto event, String reason) {
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                if (logRepo.findByIdOptional(event.eventId()).isEmpty()) {
                    persistLog(event, storeId, null, "REJECTED", reason);
                    entityManager.flush();
                }
            });
        } catch (PersistenceException ex) {
            throw new ConcurrentModificationException("Evento Edge", String.valueOf(event.eventId()));
        }
    }

    private void recordRejectionSequenced(UUID storeId, EdgeSyncEventDto event, long sequence, String reason) {
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                if (logRepo.findByIdOptional(event.eventId()).isEmpty()) {
                    persistLog(event, storeId, sequence, "REJECTED", reason);
                }
                advanceCursor(storeId, sequence);
                entityManager.flush();
            });
        } catch (PersistenceException ex) {
            throw new ConcurrentModificationException("Evento Edge", String.valueOf(event.eventId()));
        }
    }

    private void persistLog(
            EdgeSyncEventDto event,
            UUID storeId,
            Long sequence,
            String outcome,
            String rejectedReason) {
        EdgeSyncedEventLogEntity log = new EdgeSyncedEventLogEntity();
        log.eventId = event.eventId();
        log.storeId = storeId;
        log.sequence = sequence;
        log.eventType = event.eventType() != null ? event.eventType() : "UNKNOWN";
        log.aggregateId = event.aggregateId() != null ? event.aggregateId() : storeId;
        log.outcome = outcome;
        log.rejectedReason = rejectedReason;
        log.receivedAt = Instant.now();
        logRepo.persist(log);
    }

    private void advanceCursor(UUID storeId, long sequence) {
        int updated = entityManager.createNativeQuery("""
                UPDATE edge_sync_cursor
                SET last_contiguous_sequence = :seq,
                    updated_at = now()
                WHERE store_id = :storeId
                  AND last_contiguous_sequence = :expectedPrev
                """)
                .setParameter("seq", sequence)
                .setParameter("storeId", storeId)
                .setParameter("expectedPrev", sequence - 1L)
                .executeUpdate();
        if (updated == 0) {
            throw new ConcurrentModificationException("Cursor Edge", storeId.toString());
        }
    }
}

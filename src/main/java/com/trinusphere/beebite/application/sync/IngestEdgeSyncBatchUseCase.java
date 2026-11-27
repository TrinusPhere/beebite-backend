package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchRequest;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncEventDto;
import com.trinusphere.beebite.application.sync.dto.EdgeSyncRejectedEventDto;
import com.trinusphere.beebite.application.sync.port.EdgeEventInbox;
import com.trinusphere.beebite.application.sync.port.EdgeEventOutcome;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.exception.StoreNotFoundException;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncCursor;
import com.trinusphere.beebite.domain.sync.exception.InvalidSyncRequestException;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncCursorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class IngestEdgeSyncBatchUseCase {

    private static final Logger LOG = Logger.getLogger(IngestEdgeSyncBatchUseCase.class.getName());

    private final StoreRepository storeRepository;
    private final EdgeDeviceTokenValidator tokenValidator;
    private final EdgeEventInbox edgeEventInbox;
    private final EdgeSyncCursorRepository cursorRepository;

    @Inject
    public IngestEdgeSyncBatchUseCase(
            StoreRepository storeRepository,
            EdgeDeviceTokenValidator tokenValidator,
            EdgeEventInbox edgeEventInbox,
            EdgeSyncCursorRepository cursorRepository) {
        this.storeRepository = storeRepository;
        this.tokenValidator = tokenValidator;
        this.edgeEventInbox = edgeEventInbox;
        this.cursorRepository = cursorRepository;
    }

    public EdgeSyncBatchResponse execute(String deviceToken, EdgeSyncBatchRequest request) {
        if (request == null || request.storeId() == null) {
            throw new InvalidSyncRequestException("storeId é obrigatório no lote de sincronização.");
        }

        UUID storeId = request.storeId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        tokenValidator.validate(deviceToken, store);

        List<EdgeSyncEventDto> allEvents = request.events() != null ? request.events() : List.of();
        List<EdgeSyncEventDto> legacyEvents = allEvents.stream()
                .filter(e -> e != null && e.eventId() != null && e.sequence() == null)
                .toList();
        List<EdgeSyncEventDto> sequencedEvents = allEvents.stream()
                .filter(e -> e != null && e.eventId() != null && e.sequence() != null)
                .sorted(Comparator.comparingLong(EdgeSyncEventDto::sequence))
                .toList();

        List<UUID> ackedEventIds = new ArrayList<>();
        List<EdgeSyncRejectedEventDto> rejectedEvents = new ArrayList<>();
        int processedCount = 0;
        boolean requiresFullResync = false;

        // 1. Eventos legados (edges anteriores à Camada 2)
        for (EdgeSyncEventDto event : legacyEvents) {
            try {
                EdgeEventOutcome outcome = edgeEventInbox.applyLegacy(storeId, event);
                if (outcome.status() == EdgeEventOutcome.Status.APPLIED) {
                    ackedEventIds.add(event.eventId());
                    processedCount++;
                } else if (outcome.status() == EdgeEventOutcome.Status.DUPLICATE) {
                    ackedEventIds.add(event.eventId());
                } else {
                    rejectedEvents.add(new EdgeSyncRejectedEventDto(event.eventId(), outcome.reason()));
                }
            } catch (RuntimeException ex) {
                if (ackedEventIds.isEmpty() && rejectedEvents.isEmpty()) {
                    throw ex;
                }
                LOG.warning("Falha transitória parcial em evento legado Edge: " + ex.getMessage());
                break;
            }
        }

        // 2. Cursor sequenciado (ou probe com events vazio + firstAvailableSequence)
        EdgeSyncCursor cursor = null;
        if (!sequencedEvents.isEmpty()) {
            long firstSeq = request.firstAvailableSequence() != null
                    ? request.firstAvailableSequence()
                    : sequencedEvents.get(0).sequence();
            long baseline = Math.max(0L, firstSeq - 1L);
            cursor = cursorRepository.findByStoreId(storeId)
                    .orElseGet(() -> cursorRepository.save(EdgeSyncCursor.initialize(storeId, baseline)));

            if (request.firstAvailableSequence() != null
                    && cursor.hasUnrecoverableGap(request.firstAvailableSequence())) {
                long prevCursorSeq = cursor.getLastContiguousSequence();
                long newBaseline = Math.max(0L, request.firstAvailableSequence() - 1L);
                LOG.warning(() -> String.format(
                        "Gap irrecuperável detectado para loja %s (cursor=%d, firstAvailable=%d). Rebase para %d.",
                        storeId, prevCursorSeq, request.firstAvailableSequence(), newBaseline));
                cursor.rebase(newBaseline);
                cursorRepository.save(cursor);
                requiresFullResync = true;
            }

            for (EdgeSyncEventDto event : sequencedEvents) {
                long seq = event.sequence();
                if (cursor.isDuplicate(seq)) {
                    ackedEventIds.add(event.eventId());
                    continue;
                }
                if (!cursor.isNext(seq)) {
                    break;
                }

                try {
                    EdgeEventOutcome outcome = edgeEventInbox.applyInOrder(storeId, event);
                    if (outcome.status() == EdgeEventOutcome.Status.APPLIED) {
                        ackedEventIds.add(event.eventId());
                        processedCount++;
                    } else if (outcome.status() == EdgeEventOutcome.Status.DUPLICATE) {
                        ackedEventIds.add(event.eventId());
                    } else {
                        rejectedEvents.add(new EdgeSyncRejectedEventDto(event.eventId(), outcome.reason()));
                    }
                    cursor.advanceTo(seq);
                } catch (RuntimeException ex) {
                    if (ackedEventIds.isEmpty() && rejectedEvents.isEmpty()) {
                        throw ex;
                    }
                    LOG.warning("Falha transitória parcial em evento sequenciado Edge: " + ex.getMessage());
                    break;
                }
            }
        } else if (request.firstAvailableSequence() != null || allEvents.isEmpty()) {
            cursor = cursorRepository.findByStoreId(storeId).orElse(null);
            if (cursor != null && request.firstAvailableSequence() != null
                    && cursor.hasUnrecoverableGap(request.firstAvailableSequence())) {
                cursor.rebase(Math.max(0L, request.firstAvailableSequence() - 1L));
                cursorRepository.save(cursor);
                requiresFullResync = true;
            }
        }

        Instant now = Instant.now();
        store.markEdgeSynced(now);
        storeRepository.save(store);

        Long lastContiguousSeq = cursor != null ? cursor.getLastContiguousSequence() : null;
        return new EdgeSyncBatchResponse(
                processedCount,
                ackedEventIds,
                rejectedEvents,
                lastContiguousSeq,
                requiresFullResync,
                now
        );
    }
}

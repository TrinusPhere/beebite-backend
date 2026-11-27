package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeSyncBatchResponse;
import com.trinusphere.beebite.domain.sync.entity.EdgeSyncStreamState;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import com.trinusphere.beebite.domain.sync.repository.EdgeSyncStreamStateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class EdgeSyncAckHandler {

    private static final Logger LOG = Logger.getLogger(EdgeSyncAckHandler.class.getName());

    private final EdgeOutboxRepository outboxRepository;
    private final EdgeSyncStreamStateRepository streamStateRepository;

    @Inject
    public EdgeSyncAckHandler(
            EdgeOutboxRepository outboxRepository,
            EdgeSyncStreamStateRepository streamStateRepository) {
        this.outboxRepository = outboxRepository;
        this.streamStateRepository = streamStateRepository;
    }

    public int handle(UUID storeId, EdgeSyncStreamState state, EdgeSyncBatchResponse response, Instant now) {
        int reopened = 0;
        if (response != null && response.lastContiguousSequence() != null) {
            int reopenedCount = outboxRepository.reopenSyncedAfter(storeId, response.lastContiguousSequence());
            reopened = reopenedCount;
            if (reopenedCount > 0) {
                LOG.warning(() -> String.format(
                        "%d evento(s) marcados SYNCED na loja %s não constam na nuvem (cursor=%d); reabertos para reenvio.",
                        reopenedCount, storeId, response.lastContiguousSequence()));
            }
        }

        if (response != null && response.requiresFullResync()) {
            state.requestFullResync();
        }

        Long ackSeq = response != null ? response.lastContiguousSequence() : null;
        state.recordSuccess(ackSeq, now);
        streamStateRepository.save(state);
        return reopened;
    }
}

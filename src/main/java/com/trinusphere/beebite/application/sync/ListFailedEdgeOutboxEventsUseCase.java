package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.dto.EdgeOutboxFailedEventResponse;
import com.trinusphere.beebite.domain.sync.repository.EdgeOutboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ListFailedEdgeOutboxEventsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final EdgeOutboxRepository outboxRepository;

    @Inject
    public ListFailedEdgeOutboxEventsUseCase(EdgeOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    public List<EdgeOutboxFailedEventResponse> execute(UUID storeId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        return outboxRepository.findFailed(storeId, safePage, safeSize).stream()
                .map(EdgeSyncDtoMapper::toFailedResponse)
                .toList();
    }
}


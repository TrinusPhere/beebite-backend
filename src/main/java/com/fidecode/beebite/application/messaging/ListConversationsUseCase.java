package com.fidecode.beebite.application.messaging;

import com.fidecode.beebite.application.messaging.dto.ConversationResponse;
import com.fidecode.beebite.domain.messaging.repository.ConversationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ListConversationsUseCase {
    private final ConversationRepository repo;

    @Inject
    public ListConversationsUseCase(ConversationRepository repo) {
        this.repo = repo;
    }

    public List<ConversationResponse> execute(UUID storeId) {
        return repo.findAllByStoreId(storeId).stream().map(ConversationDtoMapper::toResponse).toList();
    }
}

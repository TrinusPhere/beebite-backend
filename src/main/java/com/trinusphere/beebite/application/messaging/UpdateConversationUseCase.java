package com.trinusphere.beebite.application.messaging;

import com.trinusphere.beebite.application.messaging.dto.*;
import com.trinusphere.beebite.domain.messaging.exception.ConversationNotFoundException;
import com.trinusphere.beebite.domain.messaging.repository.ConversationRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UpdateConversationUseCase {
    private final ConversationRepository repo;

    @Inject
    public UpdateConversationUseCase(ConversationRepository repo) {
        this.repo = repo;
    }

    public ConversationResponse execute(UUID storeId, UUID id, ConversationPatchRequest request) {
        var c = repo.findById(id).orElseThrow(() -> new ConversationNotFoundException(id.toString()));
        StoreGuard.enforce(c, storeId);
        if (request.status() != null)
            c.updateStatus(request.status());
        if (request.unread() != null)
            c.updateUnread(request.unread());
        return ConversationDtoMapper.toResponse(repo.save(c));
    }
}

package com.trinusphere.beebite.application.messaging;

import com.trinusphere.beebite.application.messaging.dto.*;
import com.trinusphere.beebite.domain.messaging.entity.ConversationMessage;
import com.trinusphere.beebite.domain.messaging.exception.ConversationNotFoundException;
import com.trinusphere.beebite.domain.messaging.repository.ConversationRepository;
import com.trinusphere.beebite.domain.shared.StoreGuard;
import com.trinusphere.beebite.application.shared.port.TransactionExecutor;
import com.trinusphere.beebite.application.order.port.BiteflowEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class SendConversationMessageUseCase {
    private final ConversationRepository repo;
    private final TransactionExecutor tx;
    private final BiteflowEventPublisher publisher;

    @Inject
    public SendConversationMessageUseCase(ConversationRepository repo, TransactionExecutor tx, BiteflowEventPublisher publisher) {
        this.repo = repo;
        this.tx = tx;
        this.publisher = publisher;
    }

    public ConversationResponse.MessageResponse execute(UUID storeId, UUID id, SendMessageRequest request) {
        return tx.execute(() -> {
            var c = repo.findById(id).orElseThrow(() -> new ConversationNotFoundException(id.toString()));
            StoreGuard.enforce(c, storeId);
            var m = ConversationMessage.outbound(id, request.content());
            m.markPendingIntegration();
            m = repo.saveMessage(m);
            c.waitingCustomer();
            repo.save(c);
            publisher.publishChatMessageSent(c, m);
            return ConversationDtoMapper.toResponse(m);
        });
    }
}

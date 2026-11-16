package com.trinusphere.beebite.domain.messaging.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class ConversationNotFoundException extends EntityNotFoundException {
    public ConversationNotFoundException(String id) {
        super("Conversa não encontrada: " + id);
    }
}

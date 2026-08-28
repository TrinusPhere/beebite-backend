package com.fidecode.biteflow.domain.messaging.exception;
import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;
public class ConversationNotFoundException extends EntityNotFoundException {
    public ConversationNotFoundException(String id) { super("Conversa não encontrada: " + id); }
}

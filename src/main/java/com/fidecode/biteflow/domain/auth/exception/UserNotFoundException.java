package com.fidecode.biteflow.domain.auth.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(UUID id) {
        super("Usuário com ID '" + id + "' não encontrado.");
    }

    public UserNotFoundException(String login) {
        super("Usuário com login '" + login + "' não encontrado.");
    }
}

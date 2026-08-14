package com.fidecode.biteflow.domain.auth.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("Usuário com ID '" + id + "' não encontrado.");
    }

    public UserNotFoundException(String login) {
        super("Usuário com login '" + login + "' não encontrado.");
    }
}

package com.fidecode.biteflow.domain.auth.exception;

import com.fidecode.biteflow.domain.shared.exception.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String login) {
        super("Usuário com login '" + login + "' já existe.");
    }
}

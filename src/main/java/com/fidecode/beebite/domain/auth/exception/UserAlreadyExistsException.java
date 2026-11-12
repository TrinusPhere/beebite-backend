package com.fidecode.beebite.domain.auth.exception;

import com.fidecode.beebite.domain.shared.exception.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String login) {
        super("Usuário com login '" + login + "' já existe.");
    }
}

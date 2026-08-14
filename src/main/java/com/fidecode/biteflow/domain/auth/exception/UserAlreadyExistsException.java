package com.fidecode.biteflow.domain.auth.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String login) {
        super("Usuário com login '" + login + "' já existe.");
    }
}

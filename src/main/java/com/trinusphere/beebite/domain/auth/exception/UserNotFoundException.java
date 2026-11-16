package com.trinusphere.beebite.domain.auth.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(UUID id) {
        super("Usuário com ID '" + id + "' não encontrado.");
    }

    public UserNotFoundException(String login) {
        super("Usuário com login '" + login + "' não encontrado.");
    }
}

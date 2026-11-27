package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class EdgeOutboxEventNotFoundException extends EntityNotFoundException {
    public EdgeOutboxEventNotFoundException(UUID id) {
        super("Evento de sincronização Edge não encontrado: " + id);
    }
}


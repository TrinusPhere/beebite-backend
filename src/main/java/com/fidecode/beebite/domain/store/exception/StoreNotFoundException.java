package com.fidecode.beebite.domain.store.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class StoreNotFoundException extends EntityNotFoundException {
    public StoreNotFoundException(UUID id) {
        super("Loja com ID '" + id + "' não encontrada.");
    }

    public StoreNotFoundException(String identifier) {
        super("Loja não encontrada: " + identifier);
    }
}

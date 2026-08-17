package com.fidecode.biteflow.domain.store.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class StoreNotFoundException extends EntityNotFoundException {
    public StoreNotFoundException(UUID id) {
        super("Loja com ID '" + id + "' não encontrada.");
    }
}

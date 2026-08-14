package com.fidecode.biteflow.domain.store.exception;

import java.util.UUID;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(UUID id) {
        super("Loja com ID '" + id + "' não encontrada.");
    }
}

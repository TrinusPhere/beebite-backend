package com.fidecode.biteflow.domain.delivery.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class DriverNotFoundException extends EntityNotFoundException {
    public DriverNotFoundException(String id) {
        super("Entregador não encontrado: " + id);
    }
}

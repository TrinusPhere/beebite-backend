package com.fidecode.beebite.domain.delivery.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class DriverNotFoundException extends EntityNotFoundException {
    public DriverNotFoundException(String id) {
        super("Entregador não encontrado: " + id);
    }
}

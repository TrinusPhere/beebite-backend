package com.trinusphere.beebite.domain.delivery.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class DriverNotFoundException extends EntityNotFoundException {
    public DriverNotFoundException(String id) {
        super("Entregador não encontrado: " + id);
    }
}

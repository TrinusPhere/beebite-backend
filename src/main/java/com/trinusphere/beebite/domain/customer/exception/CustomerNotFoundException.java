package com.trinusphere.beebite.domain.customer.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class CustomerNotFoundException extends EntityNotFoundException {
    public CustomerNotFoundException(String id) {
        super("Cliente não encontrado: " + id);
    }
}

package com.fidecode.beebite.domain.customer.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class CustomerNotFoundException extends EntityNotFoundException {
    public CustomerNotFoundException(String id) {
        super("Cliente não encontrado: " + id);
    }
}

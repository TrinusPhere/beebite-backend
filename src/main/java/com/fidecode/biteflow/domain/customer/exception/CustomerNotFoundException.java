package com.fidecode.biteflow.domain.customer.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class CustomerNotFoundException extends EntityNotFoundException {
    public CustomerNotFoundException(String id) {
        super("Cliente não encontrado: " + id);
    }
}

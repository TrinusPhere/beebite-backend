package com.fidecode.biteflow.domain.customer.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String id) {
        super("Cliente não encontrado: " + id);
    }
}

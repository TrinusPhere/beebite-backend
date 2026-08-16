package com.fidecode.biteflow.domain.delivery.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(String id) {
        super("Entregador não encontrado: " + id);
    }
}

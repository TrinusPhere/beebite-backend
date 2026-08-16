package com.fidecode.biteflow.domain.catalog.exception;

public class AdditionalNotFoundException extends RuntimeException {
    public AdditionalNotFoundException(String id) {
        super("Adicional não encontrado: " + id);
    }
}

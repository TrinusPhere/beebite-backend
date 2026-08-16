package com.fidecode.biteflow.domain.catalog.exception;

public class ModifierGroupNotFoundException extends RuntimeException {
    public ModifierGroupNotFoundException(String id) {
        super("Grupo de modificadores não encontrado: " + id);
    }
}

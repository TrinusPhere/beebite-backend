package com.fidecode.biteflow.domain.catalog.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class ModifierGroupNotFoundException extends EntityNotFoundException {
    public ModifierGroupNotFoundException(String id) {
        super("Grupo de modificadores não encontrado: " + id);
    }
}

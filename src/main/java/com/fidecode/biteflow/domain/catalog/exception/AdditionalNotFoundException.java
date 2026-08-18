package com.fidecode.biteflow.domain.catalog.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class AdditionalNotFoundException extends EntityNotFoundException {
    public AdditionalNotFoundException(String id) {
        super("Adicional não encontrado: " + id);
    }
}

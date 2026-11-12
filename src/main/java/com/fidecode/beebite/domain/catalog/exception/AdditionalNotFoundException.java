package com.fidecode.beebite.domain.catalog.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class AdditionalNotFoundException extends EntityNotFoundException {
    public AdditionalNotFoundException(String id) {
        super("Adicional não encontrado: " + id);
    }
}

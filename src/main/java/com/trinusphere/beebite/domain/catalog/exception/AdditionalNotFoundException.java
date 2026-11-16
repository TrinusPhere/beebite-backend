package com.trinusphere.beebite.domain.catalog.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class AdditionalNotFoundException extends EntityNotFoundException {
    public AdditionalNotFoundException(String id) {
        super("Adicional não encontrado: " + id);
    }
}

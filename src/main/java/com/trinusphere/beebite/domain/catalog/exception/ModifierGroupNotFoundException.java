package com.trinusphere.beebite.domain.catalog.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class ModifierGroupNotFoundException extends EntityNotFoundException {
    public ModifierGroupNotFoundException(String id) {
        super("Grupo de modificadores não encontrado: " + id);
    }
}

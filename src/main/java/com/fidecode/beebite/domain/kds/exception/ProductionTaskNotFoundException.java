package com.fidecode.beebite.domain.kds.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class ProductionTaskNotFoundException extends EntityNotFoundException {
    public ProductionTaskNotFoundException(String id) {
        super("Tarefa de produção não encontrada: " + id);
    }
}


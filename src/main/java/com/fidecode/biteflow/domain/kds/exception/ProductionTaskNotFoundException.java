package com.fidecode.biteflow.domain.kds.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class ProductionTaskNotFoundException extends EntityNotFoundException {
    public ProductionTaskNotFoundException(String id) {
        super("Tarefa de produção não encontrada: " + id);
    }
}


package com.trinusphere.beebite.domain.kds.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class ProductionTaskNotFoundException extends EntityNotFoundException {
    public ProductionTaskNotFoundException(String id) {
        super("Tarefa de produção não encontrada: " + id);
    }
}


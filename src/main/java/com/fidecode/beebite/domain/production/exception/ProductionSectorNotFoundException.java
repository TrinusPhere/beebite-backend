package com.fidecode.beebite.domain.production.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class ProductionSectorNotFoundException extends EntityNotFoundException {
    public ProductionSectorNotFoundException(String id) {
        super("Setor de produção não encontrado: " + id);
    }
}

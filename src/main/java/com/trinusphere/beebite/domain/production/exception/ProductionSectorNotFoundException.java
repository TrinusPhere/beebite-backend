package com.trinusphere.beebite.domain.production.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class ProductionSectorNotFoundException extends EntityNotFoundException {
    public ProductionSectorNotFoundException(String id) {
        super("Setor de produção não encontrado: " + id);
    }
}

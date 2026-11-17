package com.trinusphere.beebite.domain.platform.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class ContractNotFoundException extends EntityNotFoundException {
    public ContractNotFoundException(UUID id) {
        super("Contrato SaaS não encontrado: " + id);
    }
}


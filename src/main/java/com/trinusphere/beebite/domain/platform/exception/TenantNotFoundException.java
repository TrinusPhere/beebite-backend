package com.trinusphere.beebite.domain.platform.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class TenantNotFoundException extends EntityNotFoundException {
    public TenantNotFoundException(UUID id) {
        super("Cliente (Tenant) não encontrado: " + id);
    }
}


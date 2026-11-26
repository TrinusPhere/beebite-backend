package com.trinusphere.beebite.domain.catalog.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class CatalogNotFoundException extends EntityNotFoundException {
    public CatalogNotFoundException(String id) {
        super("Cardápio não encontrado: " + id);
    }

    public CatalogNotFoundException(UUID id) {
        this(id != null ? id.toString() : "null");
    }
}


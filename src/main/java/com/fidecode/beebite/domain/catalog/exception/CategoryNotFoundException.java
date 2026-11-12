package com.fidecode.beebite.domain.catalog.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class CategoryNotFoundException extends EntityNotFoundException {
    public CategoryNotFoundException(String id) {
        super("Categoria não encontrada: " + id);
    }
}

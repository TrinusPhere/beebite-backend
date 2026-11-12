package com.fidecode.beebite.domain.catalog.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class IngredientNotFoundException extends EntityNotFoundException {
    public IngredientNotFoundException(String id) {
        super("Ingrediente não encontrado: " + id);
    }
}

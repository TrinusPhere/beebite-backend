package com.fidecode.biteflow.domain.catalog.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class IngredientNotFoundException extends EntityNotFoundException {
    public IngredientNotFoundException(String id) {
        super("Ingrediente não encontrado: " + id);
    }
}

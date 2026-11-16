package com.trinusphere.beebite.domain.catalog.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class IngredientNotFoundException extends EntityNotFoundException {
    public IngredientNotFoundException(String id) {
        super("Ingrediente não encontrado: " + id);
    }
}

package com.fidecode.biteflow.domain.catalog.exception;

public class IngredientNotFoundException extends RuntimeException {
    public IngredientNotFoundException(String id) {
        super("Ingrediente não encontrado: " + id);
    }
}

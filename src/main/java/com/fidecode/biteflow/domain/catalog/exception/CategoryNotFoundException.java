package com.fidecode.biteflow.domain.catalog.exception;
import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;
public class CategoryNotFoundException extends EntityNotFoundException {
    public CategoryNotFoundException(String id) { super("Categoria não encontrada: " + id); }
}

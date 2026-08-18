package com.fidecode.biteflow.domain.catalog.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException(String id) {
        super("Produto não encontrado: " + id);
    }
}

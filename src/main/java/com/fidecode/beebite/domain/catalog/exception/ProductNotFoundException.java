package com.fidecode.beebite.domain.catalog.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException(String id) {
        super("Produto não encontrado: " + id);
    }
}

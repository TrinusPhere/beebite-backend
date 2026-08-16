package com.fidecode.biteflow.domain.catalog.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("Produto não encontrado: " + id);
    }
}

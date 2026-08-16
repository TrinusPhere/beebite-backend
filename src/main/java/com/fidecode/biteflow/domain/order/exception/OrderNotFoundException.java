package com.fidecode.biteflow.domain.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) {
        super("Pedido não encontrado: " + id);
    }
}

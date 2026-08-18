package com.fidecode.biteflow.domain.order.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException(String id) {
        super("Pedido não encontrado: " + id);
    }
}

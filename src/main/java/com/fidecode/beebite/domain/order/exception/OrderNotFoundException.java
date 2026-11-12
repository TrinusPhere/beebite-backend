package com.fidecode.beebite.domain.order.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException(String id) {
        super("Pedido não encontrado: " + id);
    }
}

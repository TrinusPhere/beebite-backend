package com.fidecode.beebite.domain.inventory.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class InsufficientStockException extends BusinessRuleException {
    public InsufficientStockException(String item) {
        super("Estoque insuficiente para " + item + ".");
    }
}

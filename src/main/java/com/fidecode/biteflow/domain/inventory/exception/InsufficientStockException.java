package com.fidecode.biteflow.domain.inventory.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class InsufficientStockException extends BusinessRuleException {
    public InsufficientStockException(String item) {
        super("Estoque insuficiente para " + item + ".");
    }
}

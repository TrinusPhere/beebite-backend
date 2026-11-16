package com.trinusphere.beebite.domain.inventory.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class InsufficientStockException extends BusinessRuleException {
    public InsufficientStockException(String item) {
        super("Estoque insuficiente para " + item + ".");
    }
}

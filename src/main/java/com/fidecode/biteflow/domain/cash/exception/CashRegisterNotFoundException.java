package com.fidecode.biteflow.domain.cash.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class CashRegisterNotFoundException extends EntityNotFoundException {
    public CashRegisterNotFoundException(String id) {
        super("Caixa não encontrado: " + id);
    }
}

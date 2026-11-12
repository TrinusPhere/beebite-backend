package com.fidecode.beebite.domain.cash.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class CashRegisterNotFoundException extends EntityNotFoundException {
    public CashRegisterNotFoundException(String id) {
        super("Caixa não encontrado: " + id);
    }
}

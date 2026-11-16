package com.trinusphere.beebite.domain.cash.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class CashRegisterNotFoundException extends EntityNotFoundException {
    public CashRegisterNotFoundException(String id) {
        super("Caixa não encontrado: " + id);
    }
}

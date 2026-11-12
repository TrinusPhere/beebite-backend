package com.fidecode.beebite.domain.cash.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class CashSessionNotFoundException extends EntityNotFoundException {
    public CashSessionNotFoundException(String id) {
        super("Sessão de caixa não encontrada: " + id);
    }
}

package com.fidecode.biteflow.domain.cash.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class CashSessionNotFoundException extends EntityNotFoundException {
    public CashSessionNotFoundException(String id) {
        super("Sessão de caixa não encontrada: " + id);
    }
}

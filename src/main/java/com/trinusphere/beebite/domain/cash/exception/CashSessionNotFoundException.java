package com.trinusphere.beebite.domain.cash.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class CashSessionNotFoundException extends EntityNotFoundException {
    public CashSessionNotFoundException(String id) {
        super("Sessão de caixa não encontrada: " + id);
    }
}

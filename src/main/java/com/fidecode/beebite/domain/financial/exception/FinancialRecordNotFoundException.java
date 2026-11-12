package com.fidecode.beebite.domain.financial.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

/**
 * Exceção de domínio — registro financeiro não encontrado. Herda de EntityNotFoundException (R1 — hierarquia de
 * exceções).
 */
public class FinancialRecordNotFoundException extends EntityNotFoundException {
    public FinancialRecordNotFoundException(String id) {
        super("Registro financeiro não encontrado: " + id);
    }
}

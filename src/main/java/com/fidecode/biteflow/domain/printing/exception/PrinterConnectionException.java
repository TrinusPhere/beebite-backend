package com.fidecode.biteflow.domain.printing.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

/** Falha ao entregar o documento à impressora (rede indisponível, timeout, endereço recusado). */
public class PrinterConnectionException extends BusinessRuleException {
    public PrinterConnectionException(String message) {
        super(message);
    }
}


package com.trinusphere.beebite.domain.printing.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

/** Falha ao entregar o documento à impressora (rede indisponível, timeout, endereço recusado). */
public class PrinterConnectionException extends BusinessRuleException {
    public PrinterConnectionException(String message) {
        super(message);
    }
}


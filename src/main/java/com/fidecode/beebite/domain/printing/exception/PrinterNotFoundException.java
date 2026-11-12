package com.fidecode.beebite.domain.printing.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class PrinterNotFoundException extends EntityNotFoundException {
    public PrinterNotFoundException(String id) {
        super("Impressora não encontrada: " + id);
    }
}

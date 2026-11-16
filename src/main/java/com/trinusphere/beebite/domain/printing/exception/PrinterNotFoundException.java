package com.trinusphere.beebite.domain.printing.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class PrinterNotFoundException extends EntityNotFoundException {
    public PrinterNotFoundException(String id) {
        super("Impressora não encontrada: " + id);
    }
}

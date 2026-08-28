package com.fidecode.biteflow.domain.printing.exception;
import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;
public class PrinterNotFoundException extends EntityNotFoundException {
    public PrinterNotFoundException(String id) { super("Impressora não encontrada: " + id); }
}

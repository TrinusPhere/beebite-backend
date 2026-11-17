package com.trinusphere.beebite.domain.platform.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

import java.util.UUID;

public class InvoiceNotFoundException extends EntityNotFoundException {
    public InvoiceNotFoundException(UUID id) {
        super("Fatura SaaS não encontrada: " + id);
    }
}


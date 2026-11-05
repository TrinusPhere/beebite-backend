package com.fidecode.biteflow.domain.payment.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class PaymentNotFoundException extends EntityNotFoundException {
    public PaymentNotFoundException(String id) {
        super("Pagamento não encontrado: " + id);
    }
}

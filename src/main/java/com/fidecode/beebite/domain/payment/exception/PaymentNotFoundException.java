package com.fidecode.beebite.domain.payment.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class PaymentNotFoundException extends EntityNotFoundException {
    public PaymentNotFoundException(String id) {
        super("Pagamento não encontrado: " + id);
    }
}

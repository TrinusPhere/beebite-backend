package com.trinusphere.beebite.domain.payment.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class PaymentNotFoundException extends EntityNotFoundException {
    public PaymentNotFoundException(String id) {
        super("Pagamento não encontrado: " + id);
    }
}

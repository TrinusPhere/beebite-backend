package com.fidecode.beebite.domain.order.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidPaymentMethodException extends BusinessRuleException {
    public InvalidPaymentMethodException(String value) {
        super("Forma de pagamento inválida: " + value);
    }
}

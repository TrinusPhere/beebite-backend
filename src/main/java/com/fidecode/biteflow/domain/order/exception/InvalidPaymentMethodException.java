package com.fidecode.biteflow.domain.order.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class InvalidPaymentMethodException extends BusinessRuleException {
    public InvalidPaymentMethodException(String value) {
        super("Forma de pagamento inválida: " + value);
    }
}

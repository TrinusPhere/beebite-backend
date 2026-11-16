package com.trinusphere.beebite.domain.order.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidPaymentMethodException extends BusinessRuleException {
    public InvalidPaymentMethodException(String value) {
        super("Forma de pagamento inválida: " + value);
    }
}

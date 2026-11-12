package com.fidecode.beebite.domain.order.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidOrderTransitionException extends BusinessRuleException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}

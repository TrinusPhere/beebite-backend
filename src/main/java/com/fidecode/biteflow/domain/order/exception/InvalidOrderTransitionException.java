package com.fidecode.biteflow.domain.order.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class InvalidOrderTransitionException extends BusinessRuleException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}

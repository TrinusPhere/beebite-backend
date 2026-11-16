package com.trinusphere.beebite.domain.order.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidOrderTransitionException extends BusinessRuleException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}

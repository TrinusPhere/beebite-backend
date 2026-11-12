package com.fidecode.beebite.domain.cash.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidMovementException extends BusinessRuleException {
    public InvalidMovementException(String message) {
        super(message);
    }
}

package com.fidecode.biteflow.domain.cash.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class InvalidMovementException extends BusinessRuleException {
    public InvalidMovementException(String message) {
        super(message);
    }
}

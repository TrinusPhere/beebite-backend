package com.trinusphere.beebite.domain.cash.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidMovementException extends BusinessRuleException {
    public InvalidMovementException(String message) {
        super(message);
    }
}

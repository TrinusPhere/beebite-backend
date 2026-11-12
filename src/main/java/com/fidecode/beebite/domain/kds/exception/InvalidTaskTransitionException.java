package com.fidecode.beebite.domain.kds.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidTaskTransitionException extends BusinessRuleException {
    public InvalidTaskTransitionException(String message) {
        super(message);
    }
}


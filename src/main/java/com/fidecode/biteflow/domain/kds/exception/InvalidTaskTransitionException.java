package com.fidecode.biteflow.domain.kds.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class InvalidTaskTransitionException extends BusinessRuleException {
    public InvalidTaskTransitionException(String message) {
        super(message);
    }
}


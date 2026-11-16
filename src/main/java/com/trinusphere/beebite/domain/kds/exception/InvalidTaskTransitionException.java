package com.trinusphere.beebite.domain.kds.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidTaskTransitionException extends BusinessRuleException {
    public InvalidTaskTransitionException(String message) {
        super(message);
    }
}


package com.fidecode.biteflow.domain.cash.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class SessionAlreadyClosedException extends BusinessRuleException {
    public SessionAlreadyClosedException(String message) {
        super(message);
    }
}

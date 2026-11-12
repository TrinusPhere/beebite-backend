package com.fidecode.beebite.domain.cash.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class SessionAlreadyClosedException extends BusinessRuleException {
    public SessionAlreadyClosedException(String message) {
        super(message);
    }
}

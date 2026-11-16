package com.trinusphere.beebite.domain.cash.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class SessionAlreadyClosedException extends BusinessRuleException {
    public SessionAlreadyClosedException(String message) {
        super(message);
    }
}

package com.fidecode.beebite.domain.cash.exception;

import com.fidecode.beebite.domain.shared.exception.BusinessRuleException;

public class NoOpenSessionException extends BusinessRuleException {
    public NoOpenSessionException(String message) {
        super(message);
    }
}

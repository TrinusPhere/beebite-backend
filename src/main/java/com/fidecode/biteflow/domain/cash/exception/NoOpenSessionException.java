package com.fidecode.biteflow.domain.cash.exception;

import com.fidecode.biteflow.domain.shared.exception.BusinessRuleException;

public class NoOpenSessionException extends BusinessRuleException {
    public NoOpenSessionException(String message) {
        super(message);
    }
}

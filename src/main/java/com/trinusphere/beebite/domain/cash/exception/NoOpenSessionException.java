package com.trinusphere.beebite.domain.cash.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class NoOpenSessionException extends BusinessRuleException {
    public NoOpenSessionException(String message) {
        super(message);
    }
}

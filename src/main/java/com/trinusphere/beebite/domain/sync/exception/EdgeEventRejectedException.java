package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public abstract class EdgeEventRejectedException extends BusinessRuleException {
    protected EdgeEventRejectedException(String message) {
        super(message);
    }
}


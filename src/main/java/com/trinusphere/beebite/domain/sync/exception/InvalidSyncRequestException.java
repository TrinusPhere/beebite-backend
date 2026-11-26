package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class InvalidSyncRequestException extends BusinessRuleException {
    public InvalidSyncRequestException(String message) {
        super(message);
    }
}


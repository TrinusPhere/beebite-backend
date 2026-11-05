package com.fidecode.biteflow.domain.cash.exception;

import com.fidecode.biteflow.domain.shared.exception.ConflictException;

public class ActiveSessionAlreadyExistsException extends ConflictException {
    public ActiveSessionAlreadyExistsException(String message) {
        super(message);
    }
}

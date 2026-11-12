package com.fidecode.beebite.domain.cash.exception;

import com.fidecode.beebite.domain.shared.exception.ConflictException;

public class ActiveSessionAlreadyExistsException extends ConflictException {
    public ActiveSessionAlreadyExistsException(String message) {
        super(message);
    }
}

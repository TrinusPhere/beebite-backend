package com.trinusphere.beebite.domain.cash.exception;

import com.trinusphere.beebite.domain.shared.exception.ConflictException;

public class ActiveSessionAlreadyExistsException extends ConflictException {
    public ActiveSessionAlreadyExistsException(String message) {
        super(message);
    }
}

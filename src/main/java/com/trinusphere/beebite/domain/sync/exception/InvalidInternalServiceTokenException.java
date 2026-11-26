package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.UnauthorizedException;

public class InvalidInternalServiceTokenException extends UnauthorizedException {
    public InvalidInternalServiceTokenException(String message) {
        super(message);
    }
}


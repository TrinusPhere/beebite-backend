package com.fidecode.beebite.domain.sync.exception;

import com.fidecode.beebite.domain.shared.exception.UnauthorizedException;

public class InvalidEdgeDeviceTokenException extends UnauthorizedException {
    public InvalidEdgeDeviceTokenException(String message) {
        super(message);
    }
}

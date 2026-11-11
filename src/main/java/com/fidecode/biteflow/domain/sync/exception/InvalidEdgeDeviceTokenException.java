package com.fidecode.biteflow.domain.sync.exception;

import com.fidecode.biteflow.domain.shared.exception.UnauthorizedException;

public class InvalidEdgeDeviceTokenException extends UnauthorizedException {
    public InvalidEdgeDeviceTokenException(String message) {
        super(message);
    }
}

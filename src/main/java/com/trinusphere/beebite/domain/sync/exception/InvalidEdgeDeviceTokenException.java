package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.UnauthorizedException;

public class InvalidEdgeDeviceTokenException extends UnauthorizedException {
    public InvalidEdgeDeviceTokenException(String message) {
        super(message);
    }
}

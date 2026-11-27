package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.ConflictException;

public class EdgeSyncBusyException extends ConflictException {
    public EdgeSyncBusyException(String message) {
        super(message);
    }
}


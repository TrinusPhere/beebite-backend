package com.trinusphere.beebite.domain.sync.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

import java.util.UUID;

public class EdgeOutboxEventNotRetryableException extends BusinessRuleException {
    public EdgeOutboxEventNotRetryableException(UUID id) {
        super("Apenas eventos com status FAILED podem ser reenviados manualmente: " + id);
    }
}


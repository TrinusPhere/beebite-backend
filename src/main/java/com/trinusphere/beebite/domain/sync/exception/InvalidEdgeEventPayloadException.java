package com.trinusphere.beebite.domain.sync.exception;

public class InvalidEdgeEventPayloadException extends EdgeEventRejectedException {
    public InvalidEdgeEventPayloadException(String detail) {
        super("Payload de evento Edge inválido: " + detail);
    }
}


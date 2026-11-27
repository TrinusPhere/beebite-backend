package com.trinusphere.beebite.domain.sync.exception;

public class UnsupportedEdgeEventTypeException extends EdgeEventRejectedException {
    public UnsupportedEdgeEventTypeException(String eventType) {
        super("Tipo de evento Edge não suportado: " + eventType);
    }
}


package com.trinusphere.beebite.domain.shared.exception;

/**
 * Base para exceções de conflito de estado — HTTP 409. Ex: criar recurso que já existe.
 */
public abstract class ConflictException extends DomainException {

    protected ConflictException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatus() {
        return 409;
    }

    @Override
    public String getErrorType() {
        return "Conflict";
    }
}

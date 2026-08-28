package com.fidecode.biteflow.domain.shared.exception;

/**
 * Base para exceções de "recurso não encontrado" — HTTP 404. Todas as {@code *NotFoundException} herdam desta classe.
 */
public abstract class EntityNotFoundException extends DomainException {

    protected EntityNotFoundException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatus() {
        return 404;
    }

    @Override
    public String getErrorType() {
        return "Not Found";
    }
}

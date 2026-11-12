package com.fidecode.beebite.domain.shared.exception;

/**
 * Base para exceções de autenticação — HTTP 401. Ex: credenciais inválidas.
 */
public abstract class UnauthorizedException extends DomainException {

    protected UnauthorizedException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatus() {
        return 401;
    }

    @Override
    public String getErrorType() {
        return "Unauthorized";
    }
}

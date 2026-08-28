package com.fidecode.biteflow.domain.shared.exception;

/**
 * Exceção de acesso proibido — HTTP 403. Lançada quando o recurso não pertence à store do usuário autenticado.
 */
public class ForbiddenAccessException extends DomainException {

    public ForbiddenAccessException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatus() {
        return 403;
    }

    @Override
    public String getErrorType() {
        return "Forbidden";
    }
}

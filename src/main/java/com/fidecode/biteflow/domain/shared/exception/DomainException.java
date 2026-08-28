package com.fidecode.biteflow.domain.shared.exception;

/**
 * Classe base para todas as exceções de domínio do BiteFlow.
 *
 * <p>
 * Cada subclasse carrega o HTTP status e o tipo de erro associado, permitindo que o {@code GlobalExceptionMapper}
 * resolva qualquer exceção de domínio via polimorfismo — sem cadeia de {@code instanceof}.
 *
 * <p>
 * Princípio: <b>OCP</b> — nova exceção de domínio = nova subclasse. O mapper nunca precisa ser modificado.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * HTTP status code que esta exceção representa. Ex: 404 para "não encontrado", 409 para "conflito".
     */
    public abstract int getHttpStatus();

    /**
     * Rótulo semântico do erro (usado no campo "error" da resposta JSON). Ex: "Not Found", "Conflict", "Unauthorized".
     */
    public abstract String getErrorType();
}

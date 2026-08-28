package com.fidecode.biteflow.domain.shared.exception;

/**
 * Base para exceções de regras de negócio violadas — HTTP 422. Ex: transição de status inválida no pedido.
 */
public abstract class BusinessRuleException extends DomainException {

    protected BusinessRuleException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatus() {
        return 422;
    }

    @Override
    public String getErrorType() {
        return "Unprocessable Entity";
    }
}

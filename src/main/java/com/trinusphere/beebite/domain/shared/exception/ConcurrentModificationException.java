package com.trinusphere.beebite.domain.shared.exception;

/**
 * Exceção lançada quando há conflito de atualização concorrente (otimistic locking). O repositório de infraestrutura
 * converte {@code OptimisticLockException} do JPA para esta exceção de domínio — mantendo o domínio puro (P7).
 */
public class ConcurrentModificationException extends BusinessRuleException {

    public ConcurrentModificationException(String entityName, String id) {
        super("O " + entityName + " (id=" + id + ") foi modificado por outro processo. "
                + "Recarregue e tente novamente.");
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

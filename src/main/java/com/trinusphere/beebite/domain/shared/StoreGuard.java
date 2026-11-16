package com.trinusphere.beebite.domain.shared;

import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;

import java.util.UUID;

/**
 * Utilitário de domínio para validação de multi-tenancy. Centraliza o padrão repetitivo de verificar se uma entidade
 * pertence à store do contexto atual, eliminando duplicação em todos os Use Cases.
 *
 * <p>
 * Uso:
 *
 * <pre>
 *   Order order = StoreGuard.enforce(
 *       orderRepository.findById(id).orElseThrow(...),
 *       storeId
 *   );
 * </pre>
 */
public final class StoreGuard {

    private StoreGuard() {
        // Utilitário estático — não instanciar
    }

    /**
     * Contrato funcional para qualquer entidade com storeId. As entidades de domínio podem implementar esta interface
     * para ser usadas fluentemente com {@link StoreGuard#enforce(StoreScoped, UUID)}.
     */
    public interface StoreScoped {
        UUID getStoreId();
    }

    /**
     * Valida que a entidade pertence à store esperada. Fail-fast: lança {@link ForbiddenAccessException} se não
     * pertencer.
     *
     * @param entity
     *            entidade com storeId
     * @param expectedStoreId
     *            store do contexto corrente (extraída do JWT)
     * @param <T>
     *            tipo da entidade — deve implementar {@link StoreScoped}
     *
     * @return a própria entidade (API fluente — permite encadear)
     *
     * @throws ForbiddenAccessException
     *             se storeId da entidade != expectedStoreId
     */
    public static <T extends StoreScoped> T enforce(T entity, UUID expectedStoreId) {
        if (!entity.getStoreId().equals(expectedStoreId)) {
            throw new ForbiddenAccessException("Acesso negado: recurso não pertence a esta loja.");
        }
        return entity;
    }
}

package com.trinusphere.beebite.domain.shared;

import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;

import java.util.UUID;

/**
 * Utilitário de domínio para validação de isolamento de Cardápio (Catalog).
 * Garante que uma entidade de catálogo pertença ao {@code catalogId} vinculado à loja ativa.
 */
public final class CatalogGuard {

    private CatalogGuard() {
    }

    public interface CatalogScoped {
        UUID getCatalogId();
    }

    public static <T extends CatalogScoped> T enforce(T entity, UUID expectedCatalogId) {
        if (entity == null || entity.getCatalogId() == null || !entity.getCatalogId().equals(expectedCatalogId)) {
            throw new ForbiddenAccessException("Acesso negado: item não pertence ao cardápio desta loja.");
        }
        return entity;
    }
}


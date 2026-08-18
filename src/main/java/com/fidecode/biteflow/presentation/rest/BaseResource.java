package com.fidecode.biteflow.presentation.rest;

import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

/**
 * Classe base para todos os JAX-RS Resources autenticados.
 * Centraliza a extração de contexto do JWT (storeId),
 * eliminando duplicação em todos os Resources.
 *
 * <p>Controle de acesso é feito declarativamente nas subclasses:
 * <ul>
 *   <li>{@code @RolesAllowed} — controla quais roles podem acessar o endpoint
 *   <li>{@code @PermissionsAllowed} — controla qual permissão granular é necessária
 * </ul>
 *
 * <p>Nunca declare anotações JAX-RS aqui — cada subclasse define
 * seu próprio {@code @Path}, {@code @Produces}, etc.
 */
public abstract class BaseResource {

    @Inject
    protected JsonWebToken jwt;

    /**
     * Extrai o storeId do claim JWT.
     * Fail-fast: lança {@link IllegalStateException} se o claim não existir,
     * evitando o fallback silencioso que concedia acesso à store padrão.
     *
     * @return UUID da store do usuário autenticado
     * @throws IllegalStateException se o token não contiver o claim {@code storeId}
     */
    protected UUID getStoreId() {
        String storeIdClaim = jwt.getClaim("storeId");
        if (storeIdClaim == null || storeIdClaim.isBlank()) {
            throw new IllegalStateException("Token JWT inválido: claim 'storeId' ausente.");
        }
        return UUID.fromString(storeIdClaim);
    }
}

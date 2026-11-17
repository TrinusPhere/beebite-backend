package com.trinusphere.beebite.presentation.rest;

import com.trinusphere.beebite.domain.auth.valueobject.Role;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Classe base para todos os JAX-RS Resources autenticados. Centraliza a extração de contexto do JWT (storeId),
 * eliminando duplicação em todos os Resources.
 *
 * <p>
 * Controle de acesso é feito declarativamente nas subclasses:
 * <ul>
 * <li>{@code @RolesAllowed} — controla quais roles podem acessar o endpoint
 * <li>{@code @PermissionsAllowed} — controla qual permissão granular é necessária
 * </ul>
 *
 * <p>
 * Nunca declare anotações JAX-RS aqui — cada subclasse define seu próprio {@code @Path}, {@code @Produces}, etc.
 */
public abstract class BaseResource {

    @Inject
    protected JsonWebToken jwt;

    /**
     * Extrai o storeId do claim JWT. Fail-fast: lança {@link IllegalStateException} se o claim não existir, evitando o
     * fallback silencioso que concedia acesso à store padrão.
     *
     * @return UUID da store do usuário autenticado
     *
     * @throws IllegalStateException
     *             se o token não contiver o claim {@code storeId}
     */
    protected UUID getStoreId() {
        Object raw = jwt.getClaim("storeId");
        if (raw == null) {
            throw new IllegalStateException("Token JWT inválido: claim 'storeId' ausente.");
        }
        // SmallRye JWT pode retornar JsonString ao invés de String para claims customizados
        String storeIdClaim = raw.toString().replace("\"", "");
        if (storeIdClaim.isBlank()) {
            throw new IllegalStateException("Token JWT inválido: claim 'storeId' ausente.");
        }
        return UUID.fromString(storeIdClaim);
    }

    /**
     * Extrai o userId do claim JWT.
     *
     * @return UUID do usuário autenticado
     */
    protected UUID getUserId() {
        Object raw = jwt.getClaim("userId");
        if (raw == null) {
            throw new IllegalStateException("Token JWT inválido: claim 'userId' ausente.");
        }
        String userIdClaim = raw.toString().replace("\"", "");
        if (userIdClaim.isBlank()) {
            throw new IllegalStateException("Token JWT inválido: claim 'userId' ausente.");
        }
        return UUID.fromString(userIdClaim);
    }

    /**
     * Extrai o papel principal ({@link Role}) do claim {@code groups} do JWT.
     */
    protected Role getCallerRole() {
        if (jwt == null) {
            return null;
        }
        Set<String> groups = jwt.getGroups();
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        for (Role candidate : List.of(Role.SUPER_ADMIN, Role.ADMIN, Role.GERENTE, Role.CAIXA)) {
            if (groups.contains(candidate.name())) {
                return candidate;
            }
        }
        return null;
    }
}

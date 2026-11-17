package com.trinusphere.beebite.presentation.filter;

import com.trinusphere.beebite.application.platform.CanSuperAdminMutateStoreUseCase;
import com.trinusphere.beebite.presentation.exception.ErrorResponse;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;
import java.util.UUID;

/**
 * Filtro de governança SaaS: o usuário {@code SUPER_ADMIN} da Trinusphere administra clientes/contratos
 * ({@code /api/v1/platform/**}) e sua própria conta ({@code /api/v1/auth/**}).
 *
 * <p>
 * Quando acessa lojas de clientes, atua em modo estritamente <strong>Somente Leitura</strong> (bloqueando escrita com
 * HTTP 403 Forbidden). Exceção: quando a loja pertence ao tenant matriz da própria Trinusphere
 * ({@code 00000000-0000-0000-0000-000000000001} — Loja Beta/Sandbox de testes em produção), a escrita é liberada.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SuperAdminReadOnlyStoreFilter implements ContainerRequestFilter {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final CanSuperAdminMutateStoreUseCase canSuperAdminMutateStoreUseCase;
    private final JsonWebToken jwt;

    @Inject
    public SuperAdminReadOnlyStoreFilter(
            CanSuperAdminMutateStoreUseCase canSuperAdminMutateStoreUseCase,
            JsonWebToken jwt) {
        this.canSuperAdminMutateStoreUseCase = canSuperAdminMutateStoreUseCase;
        this.jwt = jwt;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext == null || !securityContext.isUserInRole(SUPER_ADMIN_ROLE)) {
            return;
        }

        String method = requestContext.getMethod();
        if (method != null && READ_METHODS.contains(method.toUpperCase())) {
            return;
        }

        String path = requestContext.getUriInfo() != null ? requestContext.getUriInfo().getPath() : "";
        if (isAllowedWritePathForSuperAdmin(path)) {
            return;
        }

        UUID activeStoreId = extractActiveStoreId();
        if (activeStoreId != null
                && canSuperAdminMutateStoreUseCase != null
                && canSuperAdminMutateStoreUseCase.execute(activeStoreId)) {
            return;
        }

        Response forbidden = Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErrorResponse.of(
                        403,
                        "Forbidden",
                        "Ação bloqueada: o Super Administrador possui acesso apenas de visualização nas lojas dos clientes.",
                        path
                ))
                .build();

        requestContext.abortWith(forbidden);
    }

    private UUID extractActiveStoreId() {
        if (jwt == null) {
            return null;
        }
        Object raw = jwt.getClaim("storeId");
        if (raw == null) {
            return null;
        }
        String claim = raw.toString().replace("\"", "").trim();
        if (claim.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(claim);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isAllowedWritePathForSuperAdmin(String rawPath) {
        String normalized = rawPath == null ? "" : rawPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.startsWith("/api/v1/platform") || normalized.startsWith("/api/v1/auth");
    }
}

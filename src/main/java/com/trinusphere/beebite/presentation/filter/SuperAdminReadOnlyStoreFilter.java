package com.trinusphere.beebite.presentation.filter;

import com.trinusphere.beebite.presentation.exception.ErrorResponse;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;

/**
 * Filtro de governança SaaS: o usuário {@code SUPER_ADMIN} da Trinusphere administra apenas clientes/contratos
 * ({@code /api/v1/platform/**}) e sua própria conta ({@code /api/v1/auth/**}).
 *
 * <p>
 * Quando inspeciona lojas de clientes, atua em modo estritamente <strong>Somente Leitura</strong>: requisições
 * {@code GET}/{@code HEAD}/{@code OPTIONS} são permitidas, mas qualquer escrita ({@code POST}/{@code PUT}/
 * {@code PATCH}/{@code DELETE}) nos endpoints operacionais da loja é bloqueada com HTTP 403 Forbidden.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SuperAdminReadOnlyStoreFilter implements ContainerRequestFilter {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

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

        Response forbidden = Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErrorResponse.of(
                        403,
                        "Forbidden",
                        "Ação bloqueada: o Super Administrador possui acesso apenas de visualização nos dados operacionais das lojas.",
                        path
                ))
                .build();

        requestContext.abortWith(forbidden);
    }

    private boolean isAllowedWritePathForSuperAdmin(String rawPath) {
        String normalized = rawPath == null ? "" : rawPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.startsWith("/api/v1/platform") || normalized.startsWith("/api/v1/auth");
    }
}


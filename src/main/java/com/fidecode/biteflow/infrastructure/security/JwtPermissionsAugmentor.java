package com.fidecode.biteflow.infrastructure.security;

import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;

/**
 * Augmenta o {@link SecurityIdentity} com as permissões granulares do claim {@code permissions} do JWT.
 *
 * <p>Isso habilita o uso de {@code @PermissionsAllowed("pedidos")} nos Resources,
 * substituindo o {@code checkPermission()} imperativo pelo modelo declarativo do Quarkus.
 *
 * <p>ADMIN recebe TODAS as permissões do sistema automaticamente,
 * independentemente do que está no claim {@code permissions} do token.
 */
@ApplicationScoped
public class JwtPermissionsAugmentor implements SecurityIdentityAugmentor {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String PERMISSIONS_CLAIM = "permissions";

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }

        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);

        if (identity.hasRole(ADMIN_ROLE)) {
            // ADMIN recebe todas as permissões do sistema
            Permission.allValues().forEach(perm ->
                builder.addPermissionChecker(requested ->
                    Uni.createFrom().item(requested.getName().equals(perm))
                )
            );
        } else {
            // Demais roles recebem apenas as permissões do claim JWT
            extractPermissions(identity).forEach(perm ->
                builder.addPermissionChecker(requested ->
                    Uni.createFrom().item(requested.getName().equals(perm))
                )
            );
        }

        return Uni.createFrom().item(builder.build());
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractPermissions(SecurityIdentity identity) {
        if (identity.getPrincipal() instanceof JWTCallerPrincipal jwt) {
            List<String> perms = jwt.getClaim(PERMISSIONS_CLAIM);
            if (perms != null) {
                return Set.copyOf(perms);
            }
        }
        return Set.of();
    }
}

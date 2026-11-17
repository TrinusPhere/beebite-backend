package com.trinusphere.beebite.infrastructure.security;

import com.trinusphere.beebite.domain.auth.valueobject.Permission;
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
 * <p>
 * SUPER_ADMIN e ADMIN recebem TODAS as permissões do sistema automaticamente. Além disso, SUPER_ADMIN herda o papel
 * ADMIN no SecurityIdentity para poder inspecionar qualquer endpoint administrativo de loja sem duplicar anotações.
 */
@ApplicationScoped
public class JwtPermissionsAugmentor implements SecurityIdentityAugmentor {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String PERMISSIONS_CLAIM = "permissions";

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }

        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);

        if (identity.hasRole(SUPER_ADMIN_ROLE) || identity.hasRole(ADMIN_ROLE)) {
            if (identity.hasRole(SUPER_ADMIN_ROLE) && !identity.hasRole(ADMIN_ROLE)) {
                builder.addRole(ADMIN_ROLE);
            }
            Permission.allValues().forEach(perm -> builder
                    .addPermissionChecker(requested -> Uni.createFrom().item(requested.getName().equals(perm))));
        } else {
            extractPermissions(identity).forEach(perm -> builder
                    .addPermissionChecker(requested -> Uni.createFrom().item(requested.getName().equals(perm))));
        }

        return Uni.createFrom().item(builder.build());
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractPermissions(SecurityIdentity identity) {
        if (identity.getPrincipal() instanceof JWTCallerPrincipal jwt) {
            List<?> perms = jwt.getClaim(PERMISSIONS_CLAIM);
            if (perms != null) {
                return perms.stream().map(Object::toString).map(s -> s.replace("\"", ""))
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
            }
        }
        return Set.of();
    }
}

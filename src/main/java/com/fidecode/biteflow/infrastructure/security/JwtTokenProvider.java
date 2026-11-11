package com.fidecode.biteflow.infrastructure.security;

import com.fidecode.biteflow.application.auth.port.TokenProvider;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT token provider using SmallRye JWT Build. Signs with RSA private key configured in application.properties.
 */
@ApplicationScoped
public class JwtTokenProvider implements TokenProvider {

    private static final long TOKEN_LIFESPAN_SECONDS = 86400L;
    private static final String ISSUER = "https://biteflow.api";

    @Override
    public String generateToken(User user) {
        return generateTokenForStore(user, user.getStoreId(), user.getRole(), user.getPermissions());
    }

    @Override
    public String generateTokenForStore(User user, UUID targetStoreId, Role role, List<Permission> permissions) {
        Set<String> permValues = (permissions != null ? permissions : List.<Permission>of())
                .stream()
                .map(Permission::getValue)
                .collect(Collectors.toSet());

        Role effectiveRole = role != null ? role : user.getRole();

        return Jwt.issuer(ISSUER)
                .subject(user.getLogin())
                .groups(Set.of(effectiveRole.name()))
                .claim("userId", user.getId() != null ? user.getId().toString() : "")
                .claim("storeId", targetStoreId != null ? targetStoreId.toString() : "")
                .claim("name", user.getName())
                .claim("permissions", permValues)
                .expiresIn(Duration.ofSeconds(TOKEN_LIFESPAN_SECONDS))
                .sign();
    }
}

package com.fidecode.biteflow.infrastructure.security;

import com.fidecode.biteflow.application.auth.port.TokenProvider;
import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT token provider using SmallRye JWT Build.
 * Signs with RSA private key configured in application.properties.
 */
@ApplicationScoped
public class JwtTokenProvider implements TokenProvider {

    private static final long TOKEN_LIFESPAN_SECONDS = 86400L;
    private static final String ISSUER = "https://biteflow.api";

    @Override
    public String generateToken(User user) {
        Set<String> permissions = user.getPermissions().stream()
            .map(Permission::getValue)
            .collect(Collectors.toSet());

        return Jwt.issuer(ISSUER)
            .subject(user.getLogin())
            .groups(Set.of(user.getRole().name()))
            .claim("userId", user.getId() != null ? user.getId().toString() : "")
            .claim("storeId", user.getStoreId() != null ? user.getStoreId().toString() : "")
            .claim("name", user.getName())
            .claim("permissions", permissions)
            .expiresIn(Duration.ofSeconds(TOKEN_LIFESPAN_SECONDS))
            .sign();
    }
}

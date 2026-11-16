package com.trinusphere.beebite.application.auth.port;

import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.valueobject.Permission;
import com.trinusphere.beebite.domain.auth.valueobject.Role;

import java.util.List;
import java.util.UUID;

/**
 * Port (interface) para geração de tokens JWT. Implementado na camada de infraestrutura (JwtTokenProvider).
 *
 * <p>
 * Não expõe extractLogin — a extração de claims do token é responsabilidade do framework SmallRye JWT via
 * {@code @Inject JsonWebToken} na camada de apresentação, seguindo o princípio de Segregação de Interfaces (ISP).
 */
public interface TokenProvider {
    String generateToken(User user);

    default String generateTokenForStore(User user, UUID targetStoreId, Role role, List<Permission> permissions) {
        return generateToken(user);
    }
}

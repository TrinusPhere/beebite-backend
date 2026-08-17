package com.fidecode.biteflow.application.auth.port;

import com.fidecode.biteflow.domain.auth.entity.User;

/**
 * Port (interface) para geração de tokens JWT.
 * Implementado na camada de infraestrutura (JwtTokenProvider).
 *
 * <p>Não expõe extractLogin — a extração de claims do token é responsabilidade
 * do framework SmallRye JWT via {@code @Inject JsonWebToken} na camada de apresentação,
 * seguindo o princípio de Segregação de Interfaces (ISP).
 */
public interface TokenProvider {
    String generateToken(User user);
}

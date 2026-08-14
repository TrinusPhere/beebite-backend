package com.fidecode.biteflow.application.auth.port;

import com.fidecode.biteflow.domain.auth.entity.User;

/**
 * Port (interface) para geração de tokens JWT.
 * Implementado na camada de infraestrutura (JwtTokenProvider).
 */
public interface TokenProvider {
    String generateToken(User user);
    String extractLogin(String token);
}

package com.fidecode.biteflow.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * BCrypt password hashing.
 * Usa a implementação nativa do Quarkus via elytron-security-common.
 */
@ApplicationScoped
public class PasswordHasher {

    /**
     * Gera hash BCrypt da senha.
     */
    public String hash(String plainPassword) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(10));
    }

    /**
     * Verifica se a senha plain corresponde ao hash BCrypt.
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

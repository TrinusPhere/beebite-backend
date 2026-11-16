package com.trinusphere.beebite.infrastructure.security;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementação BCrypt do port {@link PasswordEncoder}. Confinada na infraestrutura — Use Cases dependem apenas da
 * interface.
 */
@ApplicationScoped
public class PasswordHasher implements PasswordEncoder {

    /**
     * {@inheritDoc} Gera hash BCrypt da senha (fator 10).
     */
    @Override
    public String hash(String plainPassword) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(10));
    }

    /**
     * {@inheritDoc} Verifica se a senha plain corresponde ao hash BCrypt armazenado.
     */
    @Override
    public boolean matches(String plainPassword, String hashedPassword) {
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

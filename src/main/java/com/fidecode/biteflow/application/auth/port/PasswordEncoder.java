package com.fidecode.biteflow.application.auth.port;

/**
 * Port (interface) para hashing e verificação de senhas.
 * Implementado na camada de infraestrutura (PasswordHasher).
 * Garante que os Use Cases dependam apenas de abstração, nunca da impl BCrypt.
 */
public interface PasswordEncoder {

    /**
     * Gera hash da senha em plain-text.
     *
     * @param plainPassword senha plain-text
     * @return hash da senha
     */
    String hash(String plainPassword);

    /**
     * Verifica se a senha plain corresponde ao hash armazenado.
     *
     * @param plainPassword  senha plain-text
     * @param hashedPassword hash armazenado
     * @return true se a senha bate com o hash
     */
    boolean matches(String plainPassword, String hashedPassword);
}

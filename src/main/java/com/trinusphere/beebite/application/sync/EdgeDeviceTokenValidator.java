package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.sync.exception.InvalidEdgeDeviceTokenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@ApplicationScoped
public class EdgeDeviceTokenValidator {

    private final PasswordEncoder passwordEncoder;

    @Inject
    public EdgeDeviceTokenValidator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void validate(String deviceToken, Store store) {
        String configuredHash = store != null ? store.getEdgeApiKeyHash() : null;
        if (deviceToken == null || deviceToken.isBlank() || configuredHash == null || configuredHash.isBlank()) {
            throw new InvalidEdgeDeviceTokenException(
                    "Token de dispositivo Edge ausente ou não configurado para esta loja.");
        }

        boolean valid;
        if (configuredHash.startsWith("$2a$") || configuredHash.startsWith("$2b$") || configuredHash.startsWith("$2y$")) {
            valid = passwordEncoder.matches(deviceToken, configuredHash);
        } else {
            valid = MessageDigest.isEqual(
                    deviceToken.getBytes(StandardCharsets.UTF_8),
                    configuredHash.getBytes(StandardCharsets.UTF_8)
            );
        }

        if (!valid) {
            throw new InvalidEdgeDeviceTokenException("Token de dispositivo Edge inválido.");
        }
    }
}


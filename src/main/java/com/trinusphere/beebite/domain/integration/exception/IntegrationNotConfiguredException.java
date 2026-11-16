package com.trinusphere.beebite.domain.integration.exception;

import com.trinusphere.beebite.domain.shared.exception.BusinessRuleException;

public class IntegrationNotConfiguredException extends BusinessRuleException {
    public IntegrationNotConfiguredException(String provider) {
        super("Integração " + provider + " não configurada.");
    }

    public IntegrationNotConfiguredException(String provider, String detail) {
        super("Integração " + provider + " não configurada: " + detail);
    }
}

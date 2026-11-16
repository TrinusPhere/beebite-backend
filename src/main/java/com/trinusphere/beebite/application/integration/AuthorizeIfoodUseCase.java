package com.trinusphere.beebite.application.integration;

import com.trinusphere.beebite.application.integration.dto.IntegrationResponse;
import com.trinusphere.beebite.domain.integration.entity.IntegrationConfig;
import com.trinusphere.beebite.domain.integration.exception.IntegrationNotConfiguredException;
import com.trinusphere.beebite.domain.integration.repository.IntegrationConfigRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class AuthorizeIfoodUseCase {
    private final IntegrationConfigRepository repo;

    @Inject
    public AuthorizeIfoodUseCase(IntegrationConfigRepository repo) {
        this.repo = repo;
    }

    public IntegrationResponse execute(UUID storeId) {
        var c = repo.findByStoreIdAndProvider(storeId, "IFOOD").orElse(new IntegrationConfig(storeId, "IFOOD"));
        if (c.getClientId() == null || c.getClientId().isBlank())
            throw new IntegrationNotConfiguredException("iFood", "Client ID ausente");
        c.markConfigured();
        return IntegrationDtoMapper.toResponse(repo.save(c));
    }
}

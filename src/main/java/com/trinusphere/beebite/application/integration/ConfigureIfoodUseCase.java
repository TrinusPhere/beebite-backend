package com.trinusphere.beebite.application.integration;

import com.trinusphere.beebite.application.integration.dto.*;
import com.trinusphere.beebite.domain.integration.entity.IntegrationConfig;
import com.trinusphere.beebite.domain.integration.repository.IntegrationConfigRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class ConfigureIfoodUseCase {
    private final IntegrationConfigRepository repo;

    @Inject
    public ConfigureIfoodUseCase(IntegrationConfigRepository repo) {
        this.repo = repo;
    }

    public IntegrationResponse execute(UUID storeId, IfoodIntegrationRequest request) {
        var c = repo.findByStoreIdAndProvider(storeId, "IFOOD").orElse(new IntegrationConfig(storeId, "IFOOD"));
        c.configureIfood(request.clientId(), request.clientSecret(), request.merchantId());
        return IntegrationDtoMapper.toResponse(repo.save(c));
    }
}

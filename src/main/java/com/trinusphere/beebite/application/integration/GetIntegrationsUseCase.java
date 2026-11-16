package com.trinusphere.beebite.application.integration;

import com.trinusphere.beebite.application.integration.dto.IntegrationsResponse;
import com.trinusphere.beebite.domain.integration.entity.IntegrationConfig;
import com.trinusphere.beebite.domain.integration.repository.IntegrationConfigRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class GetIntegrationsUseCase {
    private final IntegrationConfigRepository repo;

    @Inject
    public GetIntegrationsUseCase(IntegrationConfigRepository repo) {
        this.repo = repo;
    }

    public IntegrationsResponse execute(UUID storeId) {
        return new IntegrationsResponse(
                IntegrationDtoMapper.toResponse(repo.findByStoreIdAndProvider(storeId, "WHATSAPP")
                        .orElse(new IntegrationConfig(storeId, "WHATSAPP"))),
                IntegrationDtoMapper.toResponse(repo.findByStoreIdAndProvider(storeId, "IFOOD")
                        .orElse(new IntegrationConfig(storeId, "IFOOD"))));
    }
}

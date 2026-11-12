package com.fidecode.beebite.application.integration;

import com.fidecode.beebite.application.integration.dto.*;
import com.fidecode.beebite.domain.integration.entity.IntegrationConfig;
import com.fidecode.beebite.domain.integration.repository.IntegrationConfigRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class ConfigureWhatsappUseCase {
    private final IntegrationConfigRepository repo;

    @Inject
    public ConfigureWhatsappUseCase(IntegrationConfigRepository repo) {
        this.repo = repo;
    }

    public IntegrationResponse execute(UUID storeId, WhatsappIntegrationRequest request) {
        var c = repo.findByStoreIdAndProvider(storeId, "WHATSAPP").orElse(new IntegrationConfig(storeId, "WHATSAPP"));
        c.configureWhatsapp(request.provider(), request.phoneNumber(), request.businessAccountId(),
                request.accessToken());
        return IntegrationDtoMapper.toResponse(repo.save(c));
    }
}

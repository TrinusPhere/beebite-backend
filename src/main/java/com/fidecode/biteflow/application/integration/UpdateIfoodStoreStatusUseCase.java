package com.fidecode.biteflow.application.integration;

import com.fidecode.biteflow.application.integration.dto.IntegrationResponse;
import com.fidecode.biteflow.domain.integration.exception.IntegrationNotConfiguredException;
import com.fidecode.biteflow.domain.integration.repository.IntegrationConfigRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UpdateIfoodStoreStatusUseCase {
    private final IntegrationConfigRepository repo;

    @Inject
    public UpdateIfoodStoreStatusUseCase(IntegrationConfigRepository repo) {
        this.repo = repo;
    }

    public IntegrationResponse execute(UUID storeId, boolean open) {
        var c = repo.findByStoreIdAndProvider(storeId, "IFOOD")
                .orElseThrow(() -> new IntegrationNotConfiguredException("iFood"));
        c.changeStoreAvailability(open);
        return IntegrationDtoMapper.toResponse(repo.save(c));
    }
}

package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.UpdateTenantAccountRequest;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.exception.TenantNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateTenantAccountUseCase {

    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public UpdateTenantAccountUseCase(TenantAccountRepository tenantAccountRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public TenantAccount execute(UUID tenantId, UpdateTenantAccountRequest request) {
        TenantAccount tenant = tenantAccountRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        tenant.updateInfo(
                request.companyName(),
                request.tradeName(),
                request.ownerName(),
                request.documentNumber(),
                request.email(),
                request.phone(),
                request.notes()
        );

        return tenantAccountRepository.save(tenant);
    }
}


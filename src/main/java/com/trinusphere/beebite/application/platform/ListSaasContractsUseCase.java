package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.SaasContractResponse;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListSaasContractsUseCase {

    private final SaasContractRepository saasContractRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public ListSaasContractsUseCase(
            SaasContractRepository saasContractRepository,
            TenantAccountRepository tenantAccountRepository) {
        this.saasContractRepository = saasContractRepository;
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public List<SaasContractResponse> execute() {
        Map<UUID, String> tenantNames = tenantAccountRepository.findAll().stream()
                .collect(Collectors.toMap(TenantAccount::getId, TenantAccount::getTradeName, (a, b) -> a));

        return saasContractRepository.findAll().stream()
                .map(c -> new SaasContractResponse(
                        c.getId().toString(),
                        c.getTenantId().toString(),
                        tenantNames.getOrDefault(c.getTenantId(), "Cliente"),
                        c.getPlanName(),
                        c.getBillingCycle().name(),
                        c.getMonthlyFee(),
                        c.getSetupFee(),
                        c.getMaxStores(),
                        c.isIncludesIfood(),
                        c.isIncludesEdgeSync(),
                        c.getBillingDay(),
                        c.getStartDate(),
                        c.getEndDate(),
                        c.getStatus().name(),
                        c.getContractNotes(),
                        c.getCreatedAt()
                ))
                .toList();
    }
}


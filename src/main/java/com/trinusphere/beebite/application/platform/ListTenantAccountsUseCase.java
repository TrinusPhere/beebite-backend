package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.TenantAccountResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListTenantAccountsUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final StoreRepository storeRepository;
    private final SaasContractRepository saasContractRepository;

    @Inject
    public ListTenantAccountsUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            SaasContractRepository saasContractRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.storeRepository = storeRepository;
        this.saasContractRepository = saasContractRepository;
    }

    public List<TenantAccountResponse> execute() {
        List<TenantAccount> tenants = tenantAccountRepository.findAll();
        List<Store> allStores = storeRepository.findAll();
        List<SaasContract> allContracts = saasContractRepository.findAll();

        Map<UUID, List<Store>> storesByTenant = allStores.stream()
                .filter(s -> s.getTenantId() != null)
                .collect(Collectors.groupingBy(Store::getTenantId));

        Map<UUID, BigDecimal> mrrByTenant = allContracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .collect(Collectors.groupingBy(
                        SaasContract::getTenantId,
                        Collectors.reducing(BigDecimal.ZERO, SaasContract::getMonthlyFee, BigDecimal::add)
                ));

        return tenants.stream()
                .map(t -> {
                    List<Store> tenantStores = storesByTenant.getOrDefault(t.getId(), List.of());
                    List<TenantAccountResponse.StoreSummaryDto> storeDtos = tenantStores.stream()
                            .map(s -> new TenantAccountResponse.StoreSummaryDto(
                                    s.getId().toString(),
                                    s.getName(),
                                    s.getCnpj(),
                                    s.getSyncMode(),
                                    s.isActive(),
                                    s.getEdgeApiKeyHash(),
                                    s.getLastEdgeSyncAt()
                            ))
                            .toList();

                    return new TenantAccountResponse(
                            t.getId().toString(),
                            t.getCompanyName(),
                            t.getTradeName(),
                            t.getOwnerName(),
                            t.getDocumentNumber(),
                            t.getEmail(),
                            t.getPhone(),
                            t.getStatus().name(),
                            t.getNotes(),
                            storeDtos.size(),
                            mrrByTenant.getOrDefault(t.getId(), BigDecimal.ZERO),
                            storeDtos,
                            t.getCreatedAt()
                    );
                })
                .toList();
    }
}


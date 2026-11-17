package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.ChangeTenantStatusRequest;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.exception.TenantNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import com.trinusphere.beebite.domain.platform.valueobject.TenantStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ChangeTenantStatusUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final StoreRepository storeRepository;
    private final SaasContractRepository saasContractRepository;

    @Inject
    public ChangeTenantStatusUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            SaasContractRepository saasContractRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.storeRepository = storeRepository;
        this.saasContractRepository = saasContractRepository;
    }

    public void execute(UUID tenantId, ChangeTenantStatusRequest request) {
        TenantAccount tenant = tenantAccountRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        TenantStatus targetStatus = TenantStatus.valueOf(request.status().trim().toUpperCase());
        tenant.changeStatus(targetStatus);
        tenantAccountRepository.save(tenant);

        List<Store> tenantStores = storeRepository.findAll().stream()
                .filter(s -> tenantId.equals(s.getTenantId()))
                .toList();

        for (Store store : tenantStores) {
            if (targetStatus == TenantStatus.SUSPENDED || targetStatus == TenantStatus.CANCELLED) {
                store.deactivateStore();
            } else {
                store.activateStore();
            }
            storeRepository.save(store);
        }

        List<SaasContract> contracts = saasContractRepository.findByTenantId(tenantId);
        for (SaasContract contract : contracts) {
            contract.changeStatus(ContractStatus.valueOf(targetStatus.name()));
            saasContractRepository.save(contract);
        }
    }
}


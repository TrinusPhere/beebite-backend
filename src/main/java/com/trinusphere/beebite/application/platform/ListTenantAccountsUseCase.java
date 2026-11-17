package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.TenantAccountResponse;
import com.trinusphere.beebite.domain.auth.entity.User;
import com.trinusphere.beebite.domain.auth.repository.UserRepository;
import com.trinusphere.beebite.domain.auth.valueobject.Role;
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
    private final UserRepository userRepository;

    @Inject
    public ListTenantAccountsUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            SaasContractRepository saasContractRepository,
            UserRepository userRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.storeRepository = storeRepository;
        this.saasContractRepository = saasContractRepository;
        this.userRepository = userRepository;
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
                .map(t -> toResponse(
                        t,
                        storesByTenant.getOrDefault(t.getId(), List.of()),
                        mrrByTenant.getOrDefault(t.getId(), BigDecimal.ZERO)
                ))
                .toList();
    }

    private TenantAccountResponse toResponse(TenantAccount t, List<Store> tenantStores, BigDecimal activeMrr) {
        List<TenantAccountResponse.StoreSummaryDto> storeDtos = tenantStores.stream()
                .map(UpdatePlatformStoreUseCase::toStoreSummaryDto)
                .toList();

        String ownerLogin = resolveOwnerLogin(tenantStores);

        return new TenantAccountResponse(
                t.getId().toString(),
                t.getCompanyName(),
                t.getTradeName(),
                t.getOwnerName(),
                ownerLogin,
                t.getDocumentNumber(),
                t.getEmail(),
                t.getPhone(),
                t.getStatus().name(),
                t.getNotes(),
                storeDtos.size(),
                activeMrr,
                storeDtos,
                t.getCreatedAt()
        );
    }

    private String resolveOwnerLogin(List<Store> tenantStores) {
        if (userRepository == null || tenantStores == null || tenantStores.isEmpty()) {
            return null;
        }
        for (Store store : tenantStores) {
            List<User> storeUsers = userRepository.findAllByStoreId(store.getId());
            if (storeUsers != null && !storeUsers.isEmpty()) {
                return storeUsers.stream()
                        .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.SUPER_ADMIN)
                        .map(User::getLogin)
                        .findFirst()
                        .orElse(storeUsers.get(0).getLogin());
            }
        }
        return null;
    }
}

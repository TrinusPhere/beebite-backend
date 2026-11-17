package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.PlatformOverviewResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;
import com.trinusphere.beebite.domain.platform.valueobject.TenantStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class GetPlatformOverviewUseCase {

    private final TenantAccountRepository tenantAccountRepository;
    private final StoreRepository storeRepository;
    private final SaasContractRepository saasContractRepository;
    private final SaasInvoiceRepository saasInvoiceRepository;

    @Inject
    public GetPlatformOverviewUseCase(
            TenantAccountRepository tenantAccountRepository,
            StoreRepository storeRepository,
            SaasContractRepository saasContractRepository,
            SaasInvoiceRepository saasInvoiceRepository) {
        this.tenantAccountRepository = tenantAccountRepository;
        this.storeRepository = storeRepository;
        this.saasContractRepository = saasContractRepository;
        this.saasInvoiceRepository = saasInvoiceRepository;
    }

    public PlatformOverviewResponse execute() {
        List<TenantAccount> tenants = tenantAccountRepository.findAll();
        List<Store> stores = storeRepository.findAll();
        List<SaasContract> contracts = saasContractRepository.findAll();
        List<SaasInvoice> invoices = saasInvoiceRepository.findAll();

        int activeTenants = (int) tenants.stream()
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE || t.getStatus() == TenantStatus.TRIAL)
                .count();
        int suspendedTenants = (int) tenants.stream()
                .filter(t -> t.getStatus() == TenantStatus.SUSPENDED)
                .count();

        int edgeStores = (int) stores.stream().filter(s -> "EDGE".equalsIgnoreCase(s.getSyncMode())).count();
        int cloudStores = stores.size() - edgeStores;

        BigDecimal mrr = contracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .map(SaasContract::getMonthlyFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal received = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .map(SaasInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        BigDecimal pending = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PENDING && !i.getDueDate().isBefore(today))
                .map(SaasInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SaasInvoice> overdueList = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.OVERDUE
                        || (i.getStatus() == InvoiceStatus.PENDING && i.getDueDate().isBefore(today)))
                .toList();

        BigDecimal overdueAmount = overdueList.stream()
                .map(SaasInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PlatformOverviewResponse(
                tenants.size(),
                activeTenants,
                suspendedTenants,
                stores.size(),
                cloudStores,
                edgeStores,
                mrr,
                received,
                pending,
                overdueAmount,
                overdueList.size()
        );
    }
}


package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.InvoiceStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListSaasInvoicesUseCase {

    private final SaasInvoiceRepository saasInvoiceRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public ListSaasInvoicesUseCase(
            SaasInvoiceRepository saasInvoiceRepository,
            TenantAccountRepository tenantAccountRepository) {
        this.saasInvoiceRepository = saasInvoiceRepository;
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public List<SaasInvoiceResponse> execute(String statusFilter, UUID tenantIdFilter) {
        Map<UUID, String> tenantNames = tenantAccountRepository.findAll().stream()
                .collect(Collectors.toMap(TenantAccount::getId, TenantAccount::getTradeName, (a, b) -> a));

        List<SaasInvoice> invoices = tenantIdFilter != null
                ? saasInvoiceRepository.findByTenantId(tenantIdFilter)
                : saasInvoiceRepository.findAll();

        LocalDate today = LocalDate.now();

        return invoices.stream()
                .map(inv -> {
                    String effectiveStatus = inv.getStatus().name();
                    if (inv.getStatus() == InvoiceStatus.PENDING && inv.getDueDate().isBefore(today)) {
                        effectiveStatus = InvoiceStatus.OVERDUE.name();
                    }
                    return new SaasInvoiceResponse(
                            inv.getId().toString(),
                            inv.getContractId().toString(),
                            inv.getTenantId().toString(),
                            tenantNames.getOrDefault(inv.getTenantId(), "Cliente"),
                            inv.getReferenceMonth(),
                            inv.getDescription(),
                            inv.getAmount(),
                            inv.getDueDate(),
                            inv.getPaidAt(),
                            inv.getPaymentMethod(),
                            effectiveStatus,
                            inv.getNotes(),
                            inv.getCreatedAt()
                    );
                })
                .filter(dto -> statusFilter == null || statusFilter.isBlank()
                        || dto.status().equalsIgnoreCase(statusFilter.trim()))
                .toList();
    }
}


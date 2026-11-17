package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.exception.InvoiceNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CancelSaasInvoiceUseCase {

    private final SaasInvoiceRepository saasInvoiceRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public CancelSaasInvoiceUseCase(
            SaasInvoiceRepository saasInvoiceRepository,
            TenantAccountRepository tenantAccountRepository) {
        this.saasInvoiceRepository = saasInvoiceRepository;
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public SaasInvoiceResponse execute(UUID invoiceId, String notes) {
        SaasInvoice invoice = saasInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        invoice.cancel(notes);
        SaasInvoice saved = saasInvoiceRepository.save(invoice);

        String tradeName = tenantAccountRepository.findById(saved.getTenantId())
                .map(TenantAccount::getTradeName)
                .orElse("Cliente");

        return new SaasInvoiceResponse(
                saved.getId() != null ? saved.getId().toString() : null,
                saved.getContractId().toString(),
                saved.getTenantId().toString(),
                tradeName,
                saved.getReferenceMonth(),
                saved.getDescription(),
                saved.getAmount(),
                saved.getDueDate(),
                saved.getPaidAt(),
                saved.getPaymentMethod(),
                saved.getStatus().name(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }
}


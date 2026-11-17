package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.CreateSaasInvoiceRequest;
import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.exception.ContractNotFoundException;
import com.trinusphere.beebite.domain.platform.exception.TenantNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateSaasInvoiceUseCase {

    private final SaasInvoiceRepository saasInvoiceRepository;
    private final SaasContractRepository saasContractRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public CreateSaasInvoiceUseCase(
            SaasInvoiceRepository saasInvoiceRepository,
            SaasContractRepository saasContractRepository,
            TenantAccountRepository tenantAccountRepository) {
        this.saasInvoiceRepository = saasInvoiceRepository;
        this.saasContractRepository = saasContractRepository;
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public SaasInvoiceResponse execute(CreateSaasInvoiceRequest request) {
        TenantAccount tenant = tenantAccountRepository.findById(request.tenantId())
                .orElseThrow(() -> new TenantNotFoundException(request.tenantId()));

        saasContractRepository.findById(request.contractId())
                .orElseThrow(() -> new ContractNotFoundException(request.contractId()));

        SaasInvoice invoice = new SaasInvoice(
                request.contractId(),
                request.tenantId(),
                request.referenceMonth(),
                request.description(),
                request.amount(),
                request.dueDate()
        );

        SaasInvoice saved = saasInvoiceRepository.save(invoice);

        return new SaasInvoiceResponse(
                saved.getId() != null ? saved.getId().toString() : null,
                saved.getContractId().toString(),
                saved.getTenantId().toString(),
                tenant.getTradeName(),
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


package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.SaasInvoiceResponse;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.SaasInvoiceRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GenerateMonthlyInvoicesUseCase {

    private final SaasContractRepository saasContractRepository;
    private final SaasInvoiceRepository saasInvoiceRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public GenerateMonthlyInvoicesUseCase(
            SaasContractRepository saasContractRepository,
            SaasInvoiceRepository saasInvoiceRepository,
            TenantAccountRepository tenantAccountRepository) {
        this.saasContractRepository = saasContractRepository;
        this.saasInvoiceRepository = saasInvoiceRepository;
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public List<SaasInvoiceResponse> execute(String referenceMonth) {
        YearMonth ym = (referenceMonth != null && !referenceMonth.isBlank())
                ? YearMonth.parse(referenceMonth.trim())
                : YearMonth.now();
        String ymStr = ym.toString();

        List<SaasContract> activeContracts = saasContractRepository.findAll().stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .filter(c -> c.getMonthlyFee().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        List<SaasInvoiceResponse> generated = new ArrayList<>();

        for (SaasContract contract : activeContracts) {
            if (saasInvoiceRepository.findByContractAndMonth(contract.getId(), ymStr).isPresent()) {
                continue;
            }

            String tradeName = tenantAccountRepository.findById(contract.getTenantId())
                    .map(TenantAccount::getTradeName)
                    .orElse("Cliente");

            int day = Math.min(contract.getBillingDay(), ym.lengthOfMonth());
            LocalDate dueDate = ym.atDay(day);

            SaasInvoice invoice = new SaasInvoice(
                    contract.getId(),
                    contract.getTenantId(),
                    ymStr,
                    String.format("Mensalidade %s (%d loja(s)) — %s", contract.getPlanName(), contract.getMaxStores(), ymStr),
                    contract.getMonthlyFee(),
                    dueDate
            );

            SaasInvoice saved = saasInvoiceRepository.save(invoice);

            generated.add(new SaasInvoiceResponse(
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
            ));
        }

        return generated;
    }
}


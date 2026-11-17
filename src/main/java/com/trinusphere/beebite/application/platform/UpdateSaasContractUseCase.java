package com.trinusphere.beebite.application.platform;

import com.trinusphere.beebite.application.platform.dto.SaasContractResponse;
import com.trinusphere.beebite.application.platform.dto.UpdateSaasContractRequest;
import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.exception.ContractNotFoundException;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.domain.platform.valueobject.ContractStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateSaasContractUseCase {

    private final SaasContractRepository saasContractRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Inject
    public UpdateSaasContractUseCase(
            SaasContractRepository saasContractRepository,
            TenantAccountRepository tenantAccountRepository) {
        this.saasContractRepository = saasContractRepository;
        this.tenantAccountRepository = tenantAccountRepository;
    }

    public SaasContractResponse execute(UUID contractId, UpdateSaasContractRequest request) {
        SaasContract contract = saasContractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        ContractStatus targetStatus = (request.status() != null && !request.status().isBlank())
                ? ContractStatus.valueOf(request.status().trim().toUpperCase())
                : contract.getStatus();

        contract.updateTerms(
                request.planName(),
                request.monthlyFee(),
                request.maxStores(),
                request.includesIfood(),
                request.includesEdgeSync(),
                request.billingDay(),
                targetStatus,
                request.contractNotes()
        );

        SaasContract saved = saasContractRepository.save(contract);
        String tradeName = tenantAccountRepository.findById(saved.getTenantId())
                .map(TenantAccount::getTradeName)
                .orElse("Cliente");

        return ListSaasContractsUseCase.toResponse(saved, tradeName);
    }
}


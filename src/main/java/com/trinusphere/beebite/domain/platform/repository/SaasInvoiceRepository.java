package com.trinusphere.beebite.domain.platform.repository;

import com.trinusphere.beebite.domain.platform.entity.SaasInvoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaasInvoiceRepository {
    SaasInvoice save(SaasInvoice invoice);

    Optional<SaasInvoice> findById(UUID id);

    Optional<SaasInvoice> findByContractAndMonth(UUID contractId, String referenceMonth);

    List<SaasInvoice> findByTenantId(UUID tenantId);

    List<SaasInvoice> findAll();
}


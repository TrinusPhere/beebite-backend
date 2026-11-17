package com.trinusphere.beebite.domain.platform.repository;

import com.trinusphere.beebite.domain.platform.entity.SaasContract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaasContractRepository {
    SaasContract save(SaasContract contract);

    Optional<SaasContract> findById(UUID id);

    List<SaasContract> findByTenantId(UUID tenantId);

    List<SaasContract> findAll();
}


package com.trinusphere.beebite.domain.platform.repository;

import com.trinusphere.beebite.domain.platform.entity.TenantAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantAccountRepository {
    TenantAccount save(TenantAccount tenant);

    Optional<TenantAccount> findById(UUID id);

    List<TenantAccount> findAll();
}


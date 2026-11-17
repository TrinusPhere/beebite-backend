package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.platform.entity.TenantAccount;
import com.trinusphere.beebite.domain.platform.repository.TenantAccountRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.TenantAccountEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.TenantAccountMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheTenantAccountRepository implements TenantAccountRepository {

    @Inject
    TenantAccountPanacheRepo repo;

    @Override
    @Transactional
    public TenantAccount save(TenantAccount tenant) {
        TenantAccountEntity entity = TenantAccountMapper.toEntity(tenant);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return TenantAccountMapper.toDomain(entity);
    }

    @Override
    public Optional<TenantAccount> findById(UUID id) {
        return repo.findByIdOptional(id).map(TenantAccountMapper::toDomain);
    }

    @Override
    public List<TenantAccount> findAll() {
        return repo.find("order by createdAt desc").stream().map(TenantAccountMapper::toDomain).toList();
    }
}


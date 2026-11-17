package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.platform.entity.SaasContract;
import com.trinusphere.beebite.domain.platform.repository.SaasContractRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.SaasContractEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.SaasContractMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheSaasContractRepository implements SaasContractRepository {

    @Inject
    SaasContractPanacheRepo repo;

    @Override
    @Transactional
    public SaasContract save(SaasContract contract) {
        SaasContractEntity entity = SaasContractMapper.toEntity(contract);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return SaasContractMapper.toDomain(entity);
    }

    @Override
    public Optional<SaasContract> findById(UUID id) {
        return repo.findByIdOptional(id).map(SaasContractMapper::toDomain);
    }

    @Override
    public List<SaasContract> findByTenantId(UUID tenantId) {
        return repo.find("tenantId = ?1 order by createdAt desc", tenantId)
                .stream()
                .map(SaasContractMapper::toDomain)
                .toList();
    }

    @Override
    public List<SaasContract> findAll() {
        return repo.find("order by createdAt desc").stream().map(SaasContractMapper::toDomain).toList();
    }
}


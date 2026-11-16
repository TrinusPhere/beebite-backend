package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.repository.CashRegisterRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.CashRegisterEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.CashRegisterMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheCashRegisterRepository implements CashRegisterRepository {

    private final CashRegisterPanacheRepo repo;

    @Inject
    public PanacheCashRegisterRepository(CashRegisterPanacheRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public CashRegister save(CashRegister register) {
        CashRegisterEntity entity = CashRegisterMapper.toEntity(register);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return CashRegisterMapper.toDomain(entity);
    }

    @Override
    public Optional<CashRegister> findById(UUID id) {
        return repo.find("id = ?1 and deletedAt is null", id).firstResultOptional()
                .map(CashRegisterMapper::toDomain);
    }

    @Override
    public List<CashRegister> findAllByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 and deletedAt is null", storeId).list().stream()
                .map(CashRegisterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashRegister> findActiveByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 and active = true and deletedAt is null", storeId).list().stream()
                .map(CashRegisterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(CashRegister register) {
        if (register.getId() != null) {
            repo.update("deletedAt = now() where id = ?1", register.getId());
        }
    }
}

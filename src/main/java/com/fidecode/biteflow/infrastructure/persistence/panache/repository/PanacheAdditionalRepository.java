package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.catalog.entity.Additional;
import com.fidecode.biteflow.domain.catalog.repository.AdditionalRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.AdditionalEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.AdditionalMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheAdditionalRepository implements AdditionalRepository {

    @Inject AdditionalPanacheRepo repo;

    @Override
    public Optional<Additional> findById(UUID id) {
        return repo.findByIdOptional(id).map(AdditionalMapper::toDomain);
    }

    @Override
    public List<Additional> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(AdditionalMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Additional save(Additional additional) {
        AdditionalEntity entity = AdditionalMapper.toEntity(additional);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return AdditionalMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }
}

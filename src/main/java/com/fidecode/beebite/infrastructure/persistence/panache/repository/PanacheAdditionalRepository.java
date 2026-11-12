package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.catalog.entity.Additional;
import com.fidecode.beebite.domain.catalog.repository.AdditionalRepository;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.AdditionalEntity;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.AdditionalMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheAdditionalRepository implements AdditionalRepository {

    @Inject
    AdditionalPanacheRepo repo;

    @Override
    public Optional<Additional> findById(UUID id) {
        return repo.findByIdOptional(id).map(AdditionalMapper::toDomain);
    }

    @Override
    @Transactional
    public com.fidecode.beebite.domain.shared.PageResult<Additional> findAllByStoreId(UUID storeId, int page,
            int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Additional> content = query.list().stream().map(AdditionalMapper::toDomain).toList();
        return new com.fidecode.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
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
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            repo.getEntityManager().merge(entity);
        });
    }
}

package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.repository.ModifierGroupRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.ModifierGroupEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.ModifierGroupMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheModifierGroupRepository implements ModifierGroupRepository {

    @Inject
    ModifierGroupPanacheRepo repo;

    @Override
    @Transactional
    public Optional<ModifierGroup> findById(UUID id) {
        return repo.findByIdOptional(id).map(this::toDomainFull);
    }

    @Override
    @Transactional
    public com.trinusphere.beebite.domain.shared.PageResult<ModifierGroup> findAllByStoreId(UUID storeId, int page,
            int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<ModifierGroup> content = query.list().stream().map(ModifierGroupMapper::toDomain).toList();
        return new com.trinusphere.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public List<ModifierGroup> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream().map(this::toDomainFull).toList();
    }

    @Override
    @Transactional
    public List<ModifierGroup> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.find("id in ?1", ids).list().stream().map(this::toDomainFull).toList();
    }

    @Inject
    jakarta.persistence.EntityManager em;

    @Override
    @Transactional
    public ModifierGroup save(ModifierGroup group) {
        ModifierGroupEntity entity = ModifierGroupMapper.toEntity(group);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }

        return toDomainFull(entity);
    }

    private ModifierGroup toDomainFull(ModifierGroupEntity e) {
        return ModifierGroupMapper.toDomain(e);
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

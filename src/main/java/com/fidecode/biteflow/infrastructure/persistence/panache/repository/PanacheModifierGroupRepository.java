package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.repository.ModifierGroupRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierGroupEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.ModifierGroupMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheModifierGroupRepository implements ModifierGroupRepository {

    @Inject ModifierGroupPanacheRepo repo;

    @Override
    public Optional<ModifierGroup> findById(UUID id) {
        return repo.findByIdOptional(id).map(ModifierGroupMapper::toDomain);
    }

    @Override
    public List<ModifierGroup> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(ModifierGroupMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public ModifierGroup save(ModifierGroup group) {
        ModifierGroupEntity entity = ModifierGroupMapper.toEntity(group);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            // Delete old options and merge
            repo.getEntityManager()
                .createQuery("DELETE FROM ModifierOptionEntity WHERE modifierGroupId = :gid")
                .setParameter("gid", entity.id)
                .executeUpdate();
            entity = repo.getEntityManager().merge(entity);
        }
        return ModifierGroupMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }
}

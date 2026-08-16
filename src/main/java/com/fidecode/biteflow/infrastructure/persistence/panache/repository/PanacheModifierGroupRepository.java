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
        return repo.findByIdOptional(id).map(this::toDomainFull);
    }

    @Override
    public List<ModifierGroup> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(this::toDomainFull).toList();
    }

    @Inject jakarta.persistence.EntityManager em;

    @Override
    @Transactional
    public ModifierGroup save(ModifierGroup group) {
        ModifierGroupEntity entity = ModifierGroupMapper.toEntity(group);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        UUID groupId = entity.id;

        // Save options
        em.createQuery("DELETE FROM ModifierOptionEntity WHERE modifierGroupId = :gid")
            .setParameter("gid", groupId)
            .executeUpdate();

        if (group.getOptions() != null) {
            for (var opt : group.getOptions()) {
                com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierOptionEntity oe =
                    new com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierOptionEntity();
                oe.modifierGroupId = groupId;
                oe.name = opt.getName();
                oe.price = opt.getPrice();
                oe.sortOrder = opt.getSortOrder();
                em.persist(oe);
            }
        }

        return toDomainFull(entity);
    }

    private ModifierGroup toDomainFull(ModifierGroupEntity e) {
        List<com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierOptionEntity> optEntities = em
            .createQuery("SELECT o FROM ModifierOptionEntity o WHERE o.modifierGroupId = :gid ORDER BY o.sortOrder", com.fidecode.biteflow.infrastructure.persistence.panache.entity.ModifierOptionEntity.class)
            .setParameter("gid", e.id).getResultList();
        e.options = optEntities;
        return ModifierGroupMapper.toDomain(e);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }
}

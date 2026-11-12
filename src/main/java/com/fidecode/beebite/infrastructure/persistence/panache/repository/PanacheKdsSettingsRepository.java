package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.kds.entity.KdsSettings;
import com.fidecode.beebite.domain.kds.repository.KdsSettingsRepository;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.KdsSettingsEntity;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.KdsSettingsMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheKdsSettingsRepository implements KdsSettingsRepository {

    private final KdsSettingsPanacheRepo repo;

    @Inject
    public PanacheKdsSettingsRepository(KdsSettingsPanacheRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public KdsSettings save(KdsSettings settings) {
        KdsSettingsEntity entity = KdsSettingsMapper.toEntity(settings);
        if (repo.findById(entity.storeId) == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return KdsSettingsMapper.toDomain(entity);
    }

    @Override
    public Optional<KdsSettings> findByStoreId(UUID storeId) {
        return repo.findByIdOptional(storeId).map(KdsSettingsMapper::toDomain);
    }
}

package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.kds.entity.KdsStation;
import com.trinusphere.beebite.domain.kds.repository.KdsStationRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.KdsStationEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.KdsStationMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheKdsStationRepository implements KdsStationRepository {

    private final KdsStationPanacheRepo repo;

    @Inject
    public PanacheKdsStationRepository(KdsStationPanacheRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public KdsStation save(KdsStation station) {
        KdsStationEntity entity = KdsStationMapper.toEntity(station);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return KdsStationMapper.toDomain(entity);
    }

    @Override
    public Optional<KdsStation> findById(UUID id) {
        return repo.find("id = ?1 and deletedAt is null", id).firstResultOptional()
                .map(KdsStationMapper::toDomain);
    }

    @Override
    public List<KdsStation> findAllByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 and deletedAt is null", storeId).list().stream()
                .map(KdsStationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KdsStation> findActiveByStoreId(UUID storeId) {
        return repo.find("storeId = ?1 and active = true and deletedAt is null", storeId).list().stream()
                .map(KdsStationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(KdsStation station) {
        if (station.getId() != null) {
            repo.update("deletedAt = now() where id = ?1", station.getId());
        }
    }
}

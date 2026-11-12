package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.integration.entity.IntegrationConfig;
import com.fidecode.beebite.domain.integration.repository.IntegrationConfigRepository;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.IntegrationConfigMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class PanacheIntegrationConfigRepository implements IntegrationConfigRepository {
    @Inject
    IntegrationConfigPanacheRepo repo;

    public Optional<IntegrationConfig> findByStoreIdAndProvider(UUID s, String p) {
        return repo.find("storeId = ?1 and provider = ?2", s, p.toUpperCase()).firstResultOptional()
                .map(IntegrationConfigMapper::toDomain);
    }

    public List<IntegrationConfig> findAllByStoreId(UUID s) {
        return repo.find("storeId", s).list().stream().map(IntegrationConfigMapper::toDomain).toList();
    }

    @Transactional
    public IntegrationConfig save(IntegrationConfig c) {
        var current = repo.find("storeId = ?1 and provider = ?2", c.getStoreId(), c.getProvider()).firstResult();
        var e = IntegrationConfigMapper.toEntity(c);
        if (current == null)
            repo.persist(e);
        else {
            e.id = current.id;
            e = repo.getEntityManager().merge(e);
        }
        return IntegrationConfigMapper.toDomain(e);
    }
}

package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.catalog.entity.ProductComboComponent;
import com.fidecode.beebite.domain.catalog.repository.ProductComboComponentRepository;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.ProductComboComponentEntity;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.ProductComboComponentMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PanacheProductComboComponentRepository implements ProductComboComponentRepository {

    private final ProductComboComponentPanacheRepo repo;

    @Inject
    public PanacheProductComboComponentRepository(ProductComboComponentPanacheRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public ProductComboComponent save(ProductComboComponent component) {
        ProductComboComponentEntity entity = ProductComboComponentMapper.toEntity(component);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return ProductComboComponentMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public List<ProductComboComponent> saveAll(List<ProductComboComponent> components) {
        List<ProductComboComponent> result = new ArrayList<>();
        for (ProductComboComponent comp : components) {
            result.add(save(comp));
        }
        return result;
    }

    @Override
    public Optional<ProductComboComponent> findById(UUID id) {
        return repo.find("id = ?1 and deletedAt is null", id).firstResultOptional()
                .map(ProductComboComponentMapper::toDomain);
    }

    @Override
    public List<ProductComboComponent> findByComboProductId(UUID comboProductId) {
        return repo.find("comboProductId = ?1 and deletedAt is null", comboProductId).list().stream()
                .map(ProductComboComponentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByComboProductId(UUID comboProductId) {
        repo.update("deletedAt = ?1 where comboProductId = ?2 and deletedAt is null", java.time.LocalDateTime.now(), comboProductId);
    }
}

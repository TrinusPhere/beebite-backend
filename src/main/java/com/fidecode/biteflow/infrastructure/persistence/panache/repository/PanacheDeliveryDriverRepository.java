package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.DeliveryDriverEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.DeliveryDriverMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheDeliveryDriverRepository implements DeliveryDriverRepository {

    @Inject
    DeliveryDriverPanacheRepo repo;

    @Override
    @Transactional
    public DeliveryDriver save(DeliveryDriver driver) {
        DeliveryDriverEntity entity = DeliveryDriverMapper.toEntity(driver);
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return DeliveryDriverMapper.toDomain(entity);
    }

    @Override
    public Optional<DeliveryDriver> findById(UUID id) {
        return repo.findByIdOptional(id).map(DeliveryDriverMapper::toDomain);
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<DeliveryDriver> findAllByStoreId(UUID storeId, int page,
            int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<DeliveryDriver> content = query.list().stream().map(DeliveryDriverMapper::toDomain).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = LocalDateTime.now();
            repo.getEntityManager().merge(entity);
        });
    }
}

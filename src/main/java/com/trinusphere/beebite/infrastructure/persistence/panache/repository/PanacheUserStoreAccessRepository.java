package com.trinusphere.beebite.infrastructure.persistence.panache.repository;

import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;
import com.trinusphere.beebite.domain.auth.repository.UserStoreAccessRepository;
import com.trinusphere.beebite.infrastructure.persistence.panache.entity.UserStoreAccessEntity;
import com.trinusphere.beebite.infrastructure.persistence.panache.mapper.UserStoreAccessMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheUserStoreAccessRepository implements UserStoreAccessRepository {

    @Inject
    UserStoreAccessPanacheRepo repo;

    @Override
    public List<UserStoreAccess> findByUserId(UUID userId) {
        return repo.find("userId = ?1 order by createdAt asc", userId)
                .stream()
                .map(UserStoreAccessMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserStoreAccess> findByUserAndStore(UUID userId, UUID storeId) {
        return repo.find("userId = ?1 and storeId = ?2", userId, storeId)
                .firstResultOptional()
                .map(UserStoreAccessMapper::toDomain);
    }

    @Override
    @Transactional
    public UserStoreAccess save(UserStoreAccess access) {
        UserStoreAccessEntity entity = UserStoreAccessMapper.toEntity(access);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return UserStoreAccessMapper.toDomain(entity);
    }
}

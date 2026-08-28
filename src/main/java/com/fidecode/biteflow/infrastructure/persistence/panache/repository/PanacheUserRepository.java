package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.repository.UserRepository;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.UserEntity;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do port UserRepository (domínio).
 * Usa UserPanacheRepo internamente — padrão Repository do Panache.
 */
@ApplicationScoped
public class PanacheUserRepository implements UserRepository {

    @Inject
    UserPanacheRepo repo;

    @Override
    public Optional<User> findById(UUID id) {
        return repo.findByIdOptional(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return repo.find("login", login).firstResultOptional()
            .map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream().map(UserMapper::toDomain).toList();
    }

    @Override
    public boolean existsByLogin(String login) {
        return repo.count("login", login) > 0;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = repo.getEntityManager().merge(entity);
        }
        return UserMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            repo.getEntityManager().merge(entity);
        });
    }
}

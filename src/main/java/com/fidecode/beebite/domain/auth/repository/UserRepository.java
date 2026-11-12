package com.fidecode.beebite.domain.auth.repository;

import com.fidecode.beebite.domain.auth.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) — UserRepository.
 */
public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByLogin(String login);

    List<User> findAllByStoreId(UUID storeId);

    boolean existsByLogin(String login);

    User save(User user);

    void delete(UUID id);
}

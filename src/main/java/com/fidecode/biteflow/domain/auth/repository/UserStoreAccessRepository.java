package com.fidecode.biteflow.domain.auth.repository;

import com.fidecode.biteflow.domain.auth.entity.UserStoreAccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStoreAccessRepository {
    List<UserStoreAccess> findByUserId(UUID userId);

    Optional<UserStoreAccess> findByUserAndStore(UUID userId, UUID storeId);

    UserStoreAccess save(UserStoreAccess access);
}

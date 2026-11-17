package com.trinusphere.beebite.domain.auth.repository;

import com.trinusphere.beebite.domain.auth.entity.UserStoreAccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStoreAccessRepository {
    List<UserStoreAccess> findByUserId(UUID userId);

    List<UserStoreAccess> findByStoreId(UUID storeId);

    Optional<UserStoreAccess> findByUserAndStore(UUID userId, UUID storeId);

    UserStoreAccess save(UserStoreAccess access);

    void deleteByUserAndStore(UUID userId, UUID storeId);
}

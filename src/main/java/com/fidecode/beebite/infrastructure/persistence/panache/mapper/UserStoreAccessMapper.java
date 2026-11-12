package com.fidecode.beebite.infrastructure.persistence.panache.mapper;

import com.fidecode.beebite.domain.auth.entity.UserStoreAccess;
import com.fidecode.beebite.domain.auth.valueobject.Permission;
import com.fidecode.beebite.domain.auth.valueobject.Role;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.UserStoreAccessEntity;

import java.util.List;

public final class UserStoreAccessMapper {

    private UserStoreAccessMapper() {
    }

    public static UserStoreAccess toDomain(UserStoreAccessEntity entity) {
        if (entity == null) {
            return null;
        }
        List<Permission> perms = entity.permissions != null
                ? entity.permissions.stream().map(Permission::fromValue).toList()
                : List.of();
        return UserStoreAccess.reconstitute(
                entity.id,
                entity.userId,
                entity.storeId,
                Role.valueOf(entity.role),
                perms,
                entity.createdAt
        );
    }

    public static UserStoreAccessEntity toEntity(UserStoreAccess domain) {
        if (domain == null) {
            return null;
        }
        UserStoreAccessEntity entity = new UserStoreAccessEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.storeId = domain.getStoreId();
        entity.role = domain.getRole().name();
        entity.permissions = domain.getPermissions().stream().map(Permission::getValue).toList();
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }
}

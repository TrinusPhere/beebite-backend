package com.fidecode.biteflow.infrastructure.persistence.panache.mapper;

import com.fidecode.biteflow.domain.auth.entity.User;
import com.fidecode.biteflow.domain.auth.valueobject.Permission;
import com.fidecode.biteflow.domain.auth.valueobject.Role;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.UserEntity;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity e) {
        if (e == null)
            return null;
        List<Permission> perms = e.permissions != null ? e.permissions.stream().map(Permission::fromValue).toList()
                : List.of();
        return User.reconstitute(e.id, e.storeId, e.name, e.login, e.passwordHash, Role.valueOf(e.role), perms,
                e.active);
    }

    public static UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.id = u.getId();
        e.storeId = u.getStoreId();
        e.name = u.getName();
        e.login = u.getLogin();
        e.passwordHash = u.getPasswordHash();
        e.role = u.getRole().name();
        e.permissions = u.getPermissions().stream().map(Permission::getValue).toList();
        e.active = u.isActive();
        return e;
    }
}
